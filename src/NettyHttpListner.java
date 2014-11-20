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
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLException;

/**
 * Start ServerBootStrap in a given port for http inbound connections
 */
public class NettyHttpListner {

    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public static String HOST;
    public static int HOST_PORT;

    private int bossGroupSize;
    private int workerGroupSize;

    private int maxConnectionsQueued;

    private boolean SSL;
    
    private EventLoopGroup commonEventLoopGroup;

    public NettyHttpListner(int port) {
        this.port = port;
    }

    public NettyHttpListner() {
        System.out.println("Reading properties...");
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("res/config.properties");

            // load a properties file
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /* Backend Address*/
        HOST = prop.getProperty("host");
        HOST_PORT = Integer.parseInt(prop.getProperty("host_port"));

        /*Reverse Proxy port*/
        this.port = Integer.parseInt(prop.getProperty("port"));

        SSL = Boolean.parseBoolean(prop.getProperty("is_ssl"));
        port = SSL ? 8443 : port;

        this.bossGroupSize = Integer.parseInt(prop.getProperty("bossGroup"));
        this.workerGroupSize = Integer.parseInt(prop.getProperty("workerGroup"));

        this.maxConnectionsQueued = Integer.parseInt(prop.getProperty("connectionsQueued"));

        System.out.println("Done reading...");

    }

    public void start() {

        System.out.println("Starting the server...");
        System.out.println("Starting Inbound Http Listner on Port " + this.port);

        // Configure SSL.
        SslContext sslCtx = null;
        if (SSL) {
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            } catch (CertificateException ex) {
                Logger.getLogger(NettyHttpListner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SSLException ex) {
                Logger.getLogger(NettyHttpListner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

         commonEventLoopGroup = new NioEventLoopGroup(bossGroupSize);
//        bossGroup = new NioEventLoopGroup(bossGroupSize);
//        workerGroup = new NioEventLoopGroup(workerGroupSize);
        
        try {
            ServerBootstrap b = new ServerBootstrap();

//            b.commonEventLoopGroup(bossGroup, workerGroup)
            b.group(commonEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyHttpTransportHandlerInitializer(HOST, HOST_PORT, maxConnectionsQueued, sslCtx))
                    .childOption(ChannelOption.AUTO_READ, false);

            b.option(ChannelOption.TCP_NODELAY, true);
            b.childOption(ChannelOption.TCP_NODELAY, true);
            
            b.option(ChannelOption.SO_BACKLOG, maxConnectionsQueued);
                        
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);

            b.option(ChannelOption.SO_SNDBUF, 1048576);
            b.option(ChannelOption.SO_RCVBUF, 1048576);
            b.childOption(ChannelOption.SO_RCVBUF, 1048576);
            b.childOption(ChannelOption.SO_SNDBUF, 1048576);

            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            Channel ch = null;
            try {
                ch = b.bind(port).sync().channel();
                ch.closeFuture().sync();
                System.out.println("Inbound Listner Started");
            } catch (InterruptedException e) {
                System.out.println("Exception caught");
            }
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void destroy() {
//        bossGroup.shutdownGracefully();
//        workerGroup.shutdownGracefully();
        commonEventLoopGroup.shutdownGracefully();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        NettyHttpListner inboundHttpListner = new NettyHttpListner();
        inboundHttpListner.start();
    }
}
