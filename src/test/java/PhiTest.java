import pt.unl.fct.di.novasys.babel.core.Babel;
import pt.unl.fct.di.novasys.babel.core.GenericProtocol;
import pt.unl.fct.di.novasys.babel.exceptions.HandlerRegistrationException;
import pt.unl.fct.di.novasys.babel.exceptions.InvalidParameterException;
import pt.unl.fct.di.novasys.babel.exceptions.ProtocolAlreadyExistsException;
import pt.unl.fct.di.novasys.channel.accrual.AccrualChannel;
import pt.unl.fct.di.novasys.channel.accrual.events.PhiEvent;
import pt.unl.fct.di.novasys.channel.tcp.events.*;
import pt.unl.fct.di.novasys.network.data.Host;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Properties;

public class PhiTest {

    public static void main(String[] args) throws InvalidParameterException, IOException, ProtocolAlreadyExistsException, HandlerRegistrationException {
        Properties configProps = Babel.loadConfig(Arrays.copyOfRange(args, 0, args.length), null);
        Babel babel = Babel.getInstance();
        PhiProto phiProto = new PhiProto(configProps);
        babel.registerProtocol(phiProto);
        phiProto.init(configProps);
        babel.start();
    }


    public static class PhiProto extends GenericProtocol{

        private final String listenPort;

        public PhiProto(Properties configProps) throws IOException, HandlerRegistrationException {
            super("Phi", (short) 10);

            listenPort = configProps.getProperty("listen_port");
            Properties peerProps = new Properties();
            peerProps.setProperty(AccrualChannel.ADDRESS_KEY, "0.0.0.0");
            peerProps.setProperty(AccrualChannel.PORT_KEY, listenPort);
            peerProps.setProperty(AccrualChannel.WINDOW_SIZE_KEY, "1000");
            peerProps.setProperty(AccrualChannel.HB_INTERVAL_KEY, "50");
            peerProps.setProperty(AccrualChannel.MIN_STD_DEVIATION_KEY, "20");
            peerProps.setProperty(AccrualChannel.ACCEPTABLE_HB_PAUSE_KEY, "200");
            peerProps.setProperty(AccrualChannel.THRESHOLD_KEY, "3");
            peerProps.setProperty(AccrualChannel.PREDICT_INTERVAL_KEY, "300");
            //peerProps.put(TCPChannel.DEBUG_INTERVAL_KEY, 10000);
            int peerChannel = createChannel(AccrualChannel.NAME, peerProps);
            registerChannelEventHandler(peerChannel, InConnectionDown.EVENT_ID, this::onInConnectionDown);
            registerChannelEventHandler(peerChannel, InConnectionUp.EVENT_ID, this::onInConnectionUp);
            registerChannelEventHandler(peerChannel, OutConnectionDown.EVENT_ID, this::onOutConnectionDown);
            registerChannelEventHandler(peerChannel, OutConnectionUp.EVENT_ID, this::onOutConnectionUp);
            registerChannelEventHandler(peerChannel, OutConnectionFailed.EVENT_ID, this::onOutConnectionFailed);
            registerChannelEventHandler(peerChannel, PhiEvent.EVENT_ID, this::onPhiEvent);


        }

        @Override
        public void init(Properties configProps) throws HandlerRegistrationException, IOException {
            String mode = configProps.getProperty("mode");
            if(mode.equals("client")){
                String serverPort = configProps.getProperty("server_port");
                String serverAddr = configProps.getProperty("server_addr");
                openConnection(new Host(InetAddress.getByName(serverAddr), Integer.parseInt(serverPort)));
            }
        }

        private void onPhiEvent(PhiEvent event, int channel) {
            System.out.println(event.getValues());
        }

        protected void onOutConnectionUp(OutConnectionUp event, int channel){
            System.out.println(event);
        }

        protected void onOutConnectionDown(OutConnectionDown event, int channel){
            System.out.println(event);
        }

        protected void onOutConnectionFailed(OutConnectionFailed<Void> event, int channel){
            System.out.println(event);
        }

        private void onInConnectionDown(InConnectionDown event, int channel) {
            System.out.println(event);
        }

        private void onInConnectionUp(InConnectionUp event, int channel) {
            System.out.println(event);;
        }

    }
}

