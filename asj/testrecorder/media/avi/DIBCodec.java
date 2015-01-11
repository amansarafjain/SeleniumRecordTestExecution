package asj.testrecorder.media.avi;



import java.awt.image.WritableRaster;

import java.io.IOException;

import java.io.OutputStream;

import java.awt.Rectangle;

import asj.testrecorder.media.AbstractVideoCodec;
import asj.testrecorder.media.Buffer;
import asj.testrecorder.media.Format;
import asj.testrecorder.media.VideoFormat;
import asj.testrecorder.media.io.SeekableByteArrayOutputStream;


import java.awt.image.BufferedImage;






public class DIBCodec extends AbstractVideoCodec

{

    @Override

    public Format setInputFormat(final Format f) {

        if (f instanceof VideoFormat) {

            final VideoFormat vf = (VideoFormat)f;

            int depth = vf.getDepth();

            if (depth <= 4) {

                depth = 4;

            }

            else if (depth <= 8) {

                depth = 8;

            }

            else {

                depth = 24;

            }

            if (BufferedImage.class.isAssignableFrom(vf.getDataClass())) {

                return super.setInputFormat(new VideoFormat("image", vf.getDataClass(), vf.getWidth(), vf.getHeight(), depth));

            }

        }

        return super.setInputFormat(null);

    }

    

    @Override

    public Format setOutputFormat(final Format f) {

        if (f instanceof VideoFormat) {

            final VideoFormat vf = (VideoFormat)f;

            int depth = vf.getDepth();

            if (depth <= 4) {

                depth = 4;

            }

            else if (depth <= 8) {

                depth = 8;

            }

            else {

                depth = 24;

            }

            return super.setOutputFormat(new VideoFormat("DIB ", byte[].class, vf.getWidth(), vf.getHeight(), depth));

        }

        return super.setOutputFormat(null);

    }

    

    @Override

    public void process(final Buffer in, final Buffer out) {

        if ((in.flags & 0x2) != 0x0) {

            out.flags = 2;

            return;

        }

        out.format = this.outputFormat;

        SeekableByteArrayOutputStream tmp;

        if (out.data instanceof byte[]) {

            tmp = new SeekableByteArrayOutputStream((byte[])out.data);

        }

        else {

            tmp = new SeekableByteArrayOutputStream();

        }

        final VideoFormat vf = (VideoFormat)this.outputFormat;

        int scanlineStride;

        Rectangle r;

        if (in.data instanceof BufferedImage) {

            final BufferedImage image = (BufferedImage)in.data;

            final WritableRaster raster = image.getRaster();

            scanlineStride = raster.getSampleModel().getWidth();

            final Rectangle bounds;

            r = (bounds = raster.getBounds());

            bounds.x -= raster.getSampleModelTranslateX();

            final Rectangle rectangle = r;

            rectangle.y -= raster.getSampleModelTranslateY();

        }

        else {

            r = new Rectangle(0, 0, vf.getWidth(), vf.getHeight());

            scanlineStride = vf.getWidth();

        }

        try {

            switch (vf.getDepth()) {

                case 4: {

                    final byte[] pixels = this.getIndexed8(in);

                    if (pixels == null) {

                        out.flags = 2;

                        return;

                    }

                    this.writeKey4(tmp, pixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

                    break;

                }

                case 8: {

                    final byte[] pixels = this.getIndexed8(in);

                    if (pixels == null) {

                        out.flags = 2;

                        return;

                    }

                    this.writeKey8(tmp, pixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

                    break;

                }

                case 24: {

                    final int[] pixels2 = this.getRGB24(in);

                    if (pixels2 == null) {

                        out.flags = 2;

                        return;

                    }

                    this.writeKey24(tmp, pixels2, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

                    break;

                }

                default: {

                    out.flags = 2;

                    return;

                }

            }

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

    

    public void writeKey4(final OutputStream out, final byte[] pixels, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        final byte[] bytes = new byte[width];

        for (int y = (height - 1) * scanlineStride; y >= 0; y -= scanlineStride) {

            for (int x = offset, xx = 0, n = offset + width; x < n; x += 2, ++xx) {

                bytes[xx] = (byte)((pixels[y + x] & 0xF) << 4 | (pixels[y + x + 1] & 0xF));

            }

            out.write(bytes);

        }

    }

    

    public void writeKey8(final OutputStream out, final byte[] pixels, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        for (int y = (height - 1) * scanlineStride; y >= 0; y -= scanlineStride) {

            out.write(pixels, y + offset, width);

        }

    }

    

    public void writeKey24(final OutputStream out, final int[] pixels, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        final int w3 = width * 3;

        final byte[] bytes = new byte[w3];

        for (int xy = (height - 1) * scanlineStride + offset; xy >= offset; xy -= scanlineStride) {

            for (int x = 0, xp = 0; x < w3; x += 3, ++xp) {

                final int p = pixels[xy + xp];

                bytes[x] = (byte)p;

                bytes[x + 1] = (byte)(p >> 8);

                bytes[x + 2] = (byte)(p >> 16);

            }

            out.write(bytes);

        }

    }

}