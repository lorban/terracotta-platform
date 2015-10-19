/*
 * The contents of this file are subject to the Terracotta Public License Version
 * 2.0 (the "License"); You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://terracotta.org/legal/terracotta-public-license.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Covered Software is Connection API.
 *
 * The Initial Developer of the Covered Software is
 * Terracotta, Inc., a Software AG company
 */

package org.terracotta.voltron.proxy;

import org.junit.Test;
import org.terracotta.connection.entity.Entity;
import org.terracotta.entity.ClientCommunicator;
import org.terracotta.entity.ClientDescriptor;
import org.terracotta.entity.EndpointDelegate;
import org.terracotta.entity.EntityClientEndpoint;
import org.terracotta.entity.InvocationBuilder;
import org.terracotta.entity.InvokeFuture;
import org.terracotta.exception.EntityException;
import org.terracotta.voltron.proxy.client.ClientProxyFactory;
import org.terracotta.voltron.proxy.client.messages.MessageListener;
import org.terracotta.voltron.proxy.client.messages.ServerMessageAware;
import org.terracotta.voltron.proxy.server.ProxyInvoker;

import java.io.Serializable;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alex Snaps
 */
public class EndToEndTest {

  @Test
  public void testBothEnds() throws ExecutionException, InterruptedException {
    final Codec codec = new SerializationCodec();
    final ProxyInvoker<Comparable> proxyInvoker = new ProxyInvoker<Comparable>(Comparable.class, new Comparable() {
      public int compareTo(final Object o) {
        return 42;
      }
    }, codec);
    final EntityClientEndpoint endpoint = mock(EntityClientEndpoint.class);
    final RecordingInvocationBuilder builder = new RecordingInvocationBuilder(proxyInvoker);
    when(endpoint.beginInvoke()).thenReturn(builder);


    final Comparable proxy = ClientProxyFactory.createProxy(Comparable.class, Comparable.class, endpoint, codec);
    assertThat(proxy.compareTo("blah!"), is(42));
  }

  @Test
  public void testMessaging() throws ExecutionException, InterruptedException {
    final Codec codec = new SerializationCodec();
    final ProxyInvoker<Comparable> proxyInvoker = new ProxyInvoker<Comparable>(Comparable.class, new Comparable() {
      public int compareTo(final Object o) {
        return 42;
      }
    }, codec, String.class);
    final RecordingInvocationBuilder builder = new RecordingInvocationBuilder(proxyInvoker);
    final AtomicReference<EndpointDelegate> delegate = new AtomicReference<EndpointDelegate>();
    final EntityClientEndpoint endpoint = new EntityClientEndpoint() {
      public byte[] getEntityConfiguration() {
        throw new UnsupportedOperationException("Implement me!");
      }

      public void setDelegate(final EndpointDelegate endpointDelegate) {
        delegate.set(endpointDelegate);
      }

      public InvocationBuilder beginInvoke() {
        return builder;
      }

      public byte[] getExtendedReconnectData() {
        throw new UnsupportedOperationException("Implement me!");
      }

      public void close() {
        throw new UnsupportedOperationException("Implement me!");
      }

      public void didCloseUnexpectedly() {
        throw new UnsupportedOperationException("Implement me!");
      }
    };

    final ComparableEntity proxy = ClientProxyFactory.createEntityProxy(ComparableEntity.class, Comparable.class, endpoint, codec, String.class);
    final AtomicReference<String> messageReceived = new AtomicReference<String>();
    proxy.registerListener(new MessageListener<String>() {
      @Override
      public void onMessage(final String message) {
        messageReceived.set(message);
      }
    });
    final String message = "Hello world!";
    proxyInvoker.fireMessage(new ClientCommunicator() {
      public void sendNoResponse(final ClientDescriptor clientDescriptor, final byte[] bytes) {
        throw new UnsupportedOperationException("Implement me!");
      }

      public Future<Void> send(final ClientDescriptor clientDescriptor, final byte[] bytes) {

        final FutureTask<Void> voidFutureTask = new FutureTask<Void>(new Callable<Void>() {
          public Void call() throws Exception {
            return null;
          }
        });
        voidFutureTask.run();
        delegate.get().handleMessage(bytes);
        return voidFutureTask;
      }
    }, message, new HashSet<ClientDescriptor>() {{ add(new MyClientDescriptor()); }});

    assertThat(messageReceived.get(), equalTo(message));
  }

  @Test
  public void testClientIdSubstitution() throws ExecutionException, InterruptedException {
    final Codec codec = new SerializationCodec();
    final ProxyInvoker<ClientIdAware> proxyInvoker = new ProxyInvoker<ClientIdAware>(ClientIdAware.class, new ClientIdAware() {
      public void nothing() {
        //
      }

      public void notMuch(final Object id) {
        assertThat(id, notNullValue());
        assertThat(id, instanceOf(MyClientDescriptor.class));
      }

      public Serializable much(final Serializable foo, final Object id) {
        notMuch(id);
        return "YAY!";
      }
    }, codec);
    final EntityClientEndpoint endpoint = mock(EntityClientEndpoint.class);
    final RecordingInvocationBuilder builder = new RecordingInvocationBuilder(proxyInvoker);
    when(endpoint.beginInvoke()).thenReturn(builder);


    final ClientIdAware proxy = ClientProxyFactory.createProxy(ClientIdAware.class, ClientIdAware.class, endpoint, codec);
    proxy.nothing();
    proxy.notMuch(null);
    assertThat(proxy.much(12, 12), notNullValue());
  }

  private static class RecordingInvocationBuilder implements InvocationBuilder {
    private final ProxyInvoker<?> proxyInvoker;
    private byte[] payload;

    public RecordingInvocationBuilder(final ProxyInvoker<?> proxyInvoker) {

      this.proxyInvoker = proxyInvoker;
    }

    public InvocationBuilder ackReceived() {
      return this;
    }

    public InvocationBuilder ackCompleted() {
      return this;
    }

    public InvocationBuilder replicate(final boolean b) {
      return this;
    }

    public InvocationBuilder payload(final byte[] bytes) {
      payload = bytes;
      return this;
    }

    public InvokeFuture<byte[]> invoke() {
      final FutureTask<byte[]> futureTask = new FutureTask<byte[]>(new Callable<byte[]>() {
        public byte[] call() throws Exception {
          return proxyInvoker.invoke(new MyClientDescriptor(), payload);
        }
      });
      futureTask.run();
      return new InvokeFuture<byte[]>() {
        public boolean isDone() {
          throw new UnsupportedOperationException("Implement me!");
        }

        public byte[] get() throws InterruptedException, EntityException {
          try {
            return futureTask.get();
          } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
          }
        }

        public byte[] getWithTimeout(final long l, final TimeUnit timeUnit) throws InterruptedException, EntityException, TimeoutException {
          throw new UnsupportedOperationException("Implement me!");
        }

        public void interrupt() {
          throw new UnsupportedOperationException("Implement me!");
        }
      };
    }

  }

  private static class MyClientDescriptor implements ClientDescriptor {}

  public interface ClientIdAware {

    void nothing();

    void notMuch(@ClientId Object id);

    Serializable much(Serializable foo, @ClientId Object id);

  }

  public interface ComparableEntity extends ServerMessageAware, Entity, Comparable {

  }
}
