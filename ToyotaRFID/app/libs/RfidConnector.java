/**
 * Created by cravers on 7/19/2017.
 */


import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.annotation.Platform;


@Platform(include = "pcProxAPI.h")
public class RfidConnector {
    final static String TAG = "RfidConnector";

    static byte lastReadID[] = null;

    static {
        Loader.load();
    }

    private static native short GetActiveID32(byte[] pBuf, short wBufMaxSz);

    private static native short SetDevTypeSrch(short iSrchType);

    private static native int usbConnect();

    private static native int USBDisconnect();


    static void getActiveId32() {
        short PRXDEVTYP_USB = 0;
        SetDevTypeSrch(PRXDEVTYP_USB);
        if (usbConnect() == 0) {
            System.out.println("\nFailed to connect\n");
            return;
        }

        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        short wBufMaxSz = 32;
        byte buf[] = new byte[wBufMaxSz];
        short bits = GetActiveID32(buf, wBufMaxSz);

        if (bits == 0) {
            String errorMessage = "No id found, Please put card on the reader and " +
                    "make sure it must be configured with the card placed on it.";
        } else {
            lastReadID = null;
            int bytes_to_read = (bits + 7) / 8;
            if (bytes_to_read < 8) {
                bytes_to_read = 8;
            }
            lastReadID = buf;
            System.out.println();
        }
        USBDisconnect();
    }


}
