package org.imageio.action;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.perceptivesoftware.pie.common.data.Context;
import com.perceptivesoftware.pie.common.data.ContextShape;
import com.perceptivesoftware.pie.util.connector.AbstractAction;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;

public class ImageIORead extends AbstractAction
{
    private static final String IMAGE_URL = "ImageUrl";
    
    private static final String IMAGE_WIDTH = "ImageWidth";
    private static final String IMAGE_HEIGHT = "ImageHeight";
    private static final String IMAGE_TYPE = "ImageType";
    
    Logger logger = LoggerFactory.getLogger(ImageIORead.class);
    
    public ImageIORead() {
        super();
        IIORegistry.getDefaultInstance()
                   .registerServiceProvider(new TIFFImageReaderSpi());
    }

    private ContextShape INPUT_SHAPE = new ContextShape()
    {
        {
            addString(IMAGE_URL);
        }
    };

    private ContextShape OUTPUT_SHAPE = new ContextShape()
    {
        {
            addInt(IMAGE_WIDTH);
            addInt(IMAGE_HEIGHT);
            addInt(IMAGE_TYPE);
        }
    };

    @Override
    public Context execute(Context input) throws Exception
    {
        String url = input.getString(IMAGE_URL);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("ps-auto.proxy.lexmark.com", 80));

        // Log something to show how logging works
        logger.info(String.format("Executing action ImageIORead with url [%s]", url));

        URL urlObj = new URL(url);
        URLConnection connection = urlObj.openConnection(proxy);
        InputStream stream = connection.getInputStream();

        ImageInputStream imageStream = new MemoryCacheImageInputStream(stream);
        TIFFImageReader reader = new TIFFImageReader(new TIFFImageReaderSpi());
        reader.setInput(imageStream);
        BufferedImage image = reader.read(0);

        // BufferedImage image = ImageIO.read(stream);

        logger.info("Action ImageIORead completed.");

        // Create the output data context
        Context outputData = new Context();
        outputData.set(IMAGE_WIDTH, image.getWidth());
        outputData.set(IMAGE_HEIGHT, image.getHeight());
        outputData.set(IMAGE_TYPE, image.getType());
        return outputData;
    }

    @Override
    public ContextShape getInputs()
    {
        return INPUT_SHAPE;
    }

    @Override
    public ContextShape getOutputs()
    {
        return OUTPUT_SHAPE;
    }
}
