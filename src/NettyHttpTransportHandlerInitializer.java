/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

/**
 * register event handlers in the pipeline
 */
public class NettyHttpTransportHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private String host;
    private int port;
    private final SslContext sslCtx;
    private int connections;

    public NettyHttpTransportHandlerInitializer(String host, int port, int connections, SslContext sslCtx) {
        this.host = host;
        this.port = port;
        this.sslCtx = sslCtx;
        this.connections = connections;
    }

    /**
     *
     * @param ch
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //  System.out.println("Initlizing Source channel pool");
        ChannelPipeline p = ch.pipeline();

        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }

        p.addLast(new HttpServerCodec(102400, 102400, 102400));
        //   p.addLast(new HttpObjectAggregator(Constants.MAXIMUM_CHUNK_SIZE_AGGREGATOR));
        p.addLast(new NettyHttpTransportSourceHandler(connections));
    }
}
