import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

public class NettyTargetHandlerinitiler extends ChannelInitializer<SocketChannel> {

    private Channel inbound;

    public NettyTargetHandlerinitiler(Channel inbound) {
        this.inbound = inbound;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //Create a default pipeline implementation.
        ChannelPipeline p = ch.pipeline();

        //p.addLast("log", new LoggingHandler(LogLevel.INFO));
        //Enable HTTPS if necessary.
        p.addLast("codec", new HttpClientCodec(102400, 102400, 102400));
        //p.addLast(new HttpClientCodec());
        //p.addLast("inflater", new HttpContentDecompressor());

        //Remove the following line if you don't want automatic content decompression.
        //p.addLast("inflater", new HttpContentDecompressor());
        //Uncomment the following line if you don't want to handle HttpChunks.
        //p.addLast("aggregator", new HttpObjectAggregator(1048576));
        //p.addLast("handler", new NettyInboundHttpTargetHandler(inbound));
        p.addLast(new NettyInboundHttpTargetHandler(inbound));
    }

}
