package asj.testrecorder.media.jpeg;



import javax.imageio.ImageWriteParam;

import java.io.IOException;

import javax.imageio.metadata.IIOMetadata;

import java.util.List;

import java.awt.image.RenderedImage;

import javax.imageio.IIOImage;

import javax.imageio.ImageIO;

import javax.imageio.ImageWriter;

import asj.testrecorder.media.AbstractVideoCodec;
import asj.testrecorder.media.Buffer;
import asj.testrecorder.media.Format;
import asj.testrecorder.media.VideoFormat;
import asj.testrecorder.media.io.ByteArrayImageOutputStream;


import java.awt.image.BufferedImage;






public class JPEGCodec extends AbstractVideoCodec

{

    @Override

    public Format setInputFormat(final Format f) {

        if (f instanceof VideoFormat) {

            final VideoFormat vf = (VideoFormat)f;

            if (BufferedImage.class.isAssignableFrom(vf.getDataClass())) {

                return super.setInputFormat(new VideoFormat("image", vf.getDataClass(), vf.getWidth(), vf.getHeight(), vf.getDepth()));

            }

        }

        return super.setInputFormat(null);

    }

    

    @Override

    public Format setOutputFormat(final Format f) {

        if (f instanceof VideoFormat) {

            final VideoFormat vf = (VideoFormat)f;

            return super.setOutputFormat(new VideoFormat("MJPG", byte[].class, vf.getWidth(), vf.getHeight(), 24));

        }

        return super.setOutputFormat(null);

    }

    

    @Override

    public void process(final Buffer in, final Buffer out) {

        if ((in.flags & 0x2) != 0x0) {

            out.flags = 2;

            return;

        }

        final BufferedImage image = this.getBufferedImage(in);

        if (image == null) {

            out.flags = 2;

            return;

        }

        out.format = this.outputFormat;

        ByteArrayImageOutputStream tmp;

        if (out.data instanceof byte[]) {

            tmp = new ByteArrayImageOutputStream((byte[])out.data);

        }

        else {

            tmp = new ByteArrayImageOutputStream();

        }

        try {

            final ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/jpeg").next();

            final ImageWriteParam iwParam = iw.getDefaultWriteParam();

            iwParam.setCompressionMode(2);

            iwParam.setCompressionQuality(this.quality);

            iw.setOutput(tmp);

            final IIOImage img = new IIOImage(image, null, null);

            iw.write(null, img, iwParam);

            iw.dispose();

            out.flags = 16;

            out.data = tmp.getBuffer();

            out.offset = 0;

            out.length = (int)tmp.getStreamPosition();

        }

        catch (IOException ex) {

            ex.printStackTrace();

            out.flags = 2;

        }

    }

}