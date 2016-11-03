import com.jhlabs.image.BlurFilter;
import com.jhlabs.image.GaussianFilter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;



/**
 * This Java program uses the Spark web application framework to run a webserver.
 * See http://sparkjava.com/ for details on Spark.
 */
public class Main {
    public static void main(String... args) throws Exception {

        // Tell Spark to use the Environment variable "PORT" set by Heroku. If no PORT variable is set, default to port 5000.
        int port = System.getenv("PORT") == null ? 5000 : Integer.valueOf(System.getenv("PORT"));
        port(port);


        get("/", (req, res) -> "Hello!");

        // matches "GET /hello/foo" and "GET /hello/bar"
        // request.params(":name") is 'foo' or 'bar'
        get("/hello/:name", (request, response) -> "Hello: " + request.params(":name"));

        post("/process-image", (request, response) -> {
            String img = request.queryParams("myimage");
            System.out.print(img);
            if (img == null || Objects.equals(img, "")) {
                return "Invalid request, no image was sent to server!";
            }

            BufferedImage image = decodeToImage(img);
            return processImage(image);
        });
    }

    private static String processImage(BufferedImage image) {
        GaussianFilter gfilter = new GaussianFilter(12);
        BufferedImage filteredImage  = gfilter.filter(image, null);
        return encodeToString(filteredImage, "png");
    }

    private static String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String output = null;
        try {
            if (imageString != null) {
                output = URLEncoder.encode(imageString, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return output;
    }

    private static BufferedImage decodeToImage(String imageString) {

        BufferedImage image = null;
        byte[] imageByte;

        System.out.print("decoding...."+imageString);
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }
}