import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class LoganParser {
    private static final String ALGORITHM = "AES";
    private static final String ALGORITHM_TYPE = "AES/CBC/NoPadding";
    private Cipher mDecryptCipher;
    private byte[] mEncryptKey16; //128位ase加密Key
    private byte[] mEncryptIv16; //128位aes加密IV

    public LoganParser(byte[] encryptKey16, byte[] encryptIv16) {
        mEncryptKey16 = encryptKey16;
        mEncryptIv16 = encryptIv16;
        initEncrypt();
    }

    private void initEncrypt() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(mEncryptKey16, ALGORITHM);
        try {
            mDecryptCipher = Cipher.getInstance(ALGORITHM_TYPE);
            mDecryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(mEncryptIv16));
//        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parse(InputStream is, OutputStream os) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int t = 0;
        try {
            while ((t = is.read(buffer)) >= 0) {
                bos.write(buffer, 0, t);
                bos.flush();
            }
            byte[] content = bos.toByteArray();
            for (int i = 0; i < content.length; i++) {
                byte start = content[i];
                if (start == '\1') {
                    i++;
                    int length = (content[i] & 0xFF) << 24 |
                            (content[i + 1] & 0xFF) << 16 |
                            (content[i + 2] & 0xFF) << 8 |
                            (content[i + 3] & 0xFF);
                    i += 3;
                    int type;
                    if (length > 0) {
                        int temp = i + length + 1;
                        if (content.length - i - 1 == length) { //异常
                            type = 0;
                        } else if (content.length - i - 1 > length && '\0' == content[temp]) {
                            type = 1;
                        } else if (content.length - i - 1 > length && '\1' == content[temp]) { //异常
                            type = 2;
                        } else {
                            i -= 4;
                            continue;
                        }
                        byte[] dest = new byte[length];
                        System.arraycopy(content, i + 1, dest, 0, length);
                        ByteArrayOutputStream uncompressBytesArray = new ByteArrayOutputStream();
                        InflaterInputStream inflaterOs = null;
                        byte[] uncompressByte;
                        try {
                            uncompressBytesArray.reset();
                            inflaterOs = new GZIPInputStream(new CipherInputStream(new ByteArrayInputStream(dest), mDecryptCipher));
                            int e = 0;
                            while ((e = inflaterOs.read(buffer)) >= 0) {
                                uncompressBytesArray.write(buffer, 0, e);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        uncompressByte = uncompressBytesArray.toByteArray();
                        uncompressBytesArray.reset();
                        os.write(uncompressByte);
                        if (inflaterOs != null)
                            inflaterOs.close();
                        i += length;
                        if (type == 1) {
                            i++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}