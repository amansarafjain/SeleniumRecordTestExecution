package asj.testrecorder.media.image;



import java.awt.Graphics2D;

import java.awt.image.DataBufferInt;

import java.awt.image.ColorModel;

import java.awt.image.PixelGrabber;

import java.awt.Graphics;

import java.awt.GraphicsConfiguration;

import java.awt.GraphicsDevice;

import java.awt.GraphicsEnvironment;

import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

import java.awt.image.WritableRaster;

import java.util.Hashtable;

import java.awt.Point;

import java.awt.image.Raster;

import java.awt.image.BufferedImage;

import java.awt.image.RenderedImage;

import java.awt.Toolkit;

import java.net.URL;

import java.awt.Image;



public class Images

{

    public static Image createImage(final Class baseClass, final String location) {

        final URL resource = baseClass.getResource(location);

        if (resource == null) {

            System.err.println("Warning: Images.createImage no resource found for " + baseClass + " " + location);

            return null;

        }

        return createImage(resource);

    }

    

    public static Image createImage(final URL resource) {

        final Image image = Toolkit.getDefaultToolkit().createImage(resource);

        return image;

    }

    

    public static BufferedImage toBufferedImage(final RenderedImage rImg) {

        BufferedImage image;

        if (rImg instanceof BufferedImage) {

            image = (BufferedImage)rImg;

        }

        else {

            final Raster r = rImg.getData();

            final WritableRaster wr = Raster.createWritableRaster(r.getSampleModel(), null);

            rImg.copyData(wr);

            image = new BufferedImage(rImg.getColorModel(), wr, rImg.getColorModel().isAlphaPremultiplied(), null);

        }

        return image;

    }

    

    public static BufferedImage toBufferedImage(Image image) {

        if (image instanceof BufferedImage) {

            return (BufferedImage)image;

        }

        image = new ImageIcon(image).getImage();

        BufferedImage bimage = null;

        if (System.getProperty("java.version").startsWith("1.4.1_")) {

            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), 2);

        }

        else {

            boolean hasAlpha;

            try {

                hasAlpha = hasAlpha(image);

            }

            catch (IllegalAccessError e) {

                hasAlpha = true;

            }

            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            try {

                int transparency = 1;

                if (hasAlpha) {

                    transparency = 3;

                }

                final GraphicsDevice gs = ge.getDefaultScreenDevice();

                final GraphicsConfiguration gc = gs.getDefaultConfiguration();

                bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);

            }

            catch (Exception ex) {}

            if (bimage == null) {

                int type = 1;

                if (hasAlpha) {

                    type = 2;

                }

                bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);

            }

        }

        final Graphics g = bimage.createGraphics();

        g.drawImage(image, 0, 0, null);

        g.dispose();

        return bimage;

    }

    

    public static boolean hasAlpha(final Image image) {

        if (image instanceof BufferedImage) {

            final BufferedImage bimage = (BufferedImage)image;

            return bimage.getColorModel().hasAlpha();

        }

        final PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);

        try {

            pg.grabPixels();

        }

        catch (InterruptedException ex) {}

        final ColorModel cm = pg.getColorModel();

        return cm.hasAlpha();

    }

    

    public static BufferedImage[] split(final Image image, final int count, final boolean isHorizontal) {

        final BufferedImage src = toBufferedImage(image);

        if (count == 1) {

            return new BufferedImage[] { src };

        }

        final BufferedImage[] parts = new BufferedImage[count];

        for (int i = 0; i < count; ++i) {

            if (isHorizontal) {

                parts[i] = src.getSubimage(src.getWidth() / count * i, 0, src.getWidth() / count, src.getHeight());

            }

            else {

                parts[i] = src.getSubimage(0, src.getHeight() / count * i, src.getWidth(), src.getHeight() / count);

            }

        }

        return parts;

    }

    

    public static BufferedImage toIntImage(final BufferedImage img) {

        if (img.getRaster().getDataBuffer() instanceof DataBufferInt) {

            return img;

        }

        final BufferedImage intImg = new BufferedImage(img.getWidth(), img.getHeight(), 1);

        final Graphics2D g = intImg.createGraphics();

        g.drawImage(img, 0, 0, null);

        g.dispose();

        return intImg;

    }

}