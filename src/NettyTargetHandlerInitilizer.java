import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

public class NettyTargetHandlerInitilizer extends ChannelInitializer<SocketChannel> {

    private Channel inbound;

    public NettyTargetHandlerInitilizer(Channel inbound) {
        this.inbound = inbound;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //Create a default pipeline implementation.
        ChannelPipeline p = ch.pipeline();

        //Enable HTTPS if necessary.
        p.addLast("codec", new HttpClientCodec(102400, 102400, 102400));
        p.addLast(new NettyInboundHttpTargetHandler(inbound));
    }
}
