package asj.testrecorder.media.avi;



import java.nio.ByteOrder;

import java.io.OutputStream;

import java.awt.image.WritableRaster;

import java.io.IOException;

import javax.imageio.stream.ImageOutputStream;

import java.awt.Rectangle;

import asj.testrecorder.media.AbstractVideoCodec;
import asj.testrecorder.media.Buffer;
import asj.testrecorder.media.Format;
import asj.testrecorder.media.VideoFormat;
import asj.testrecorder.media.io.ByteArrayImageOutputStream;


import java.awt.image.BufferedImage;






public class RunLengthCodec extends AbstractVideoCodec

{

    private byte[] previousPixels;

    

    @Override

    public Format setInputFormat(final Format f) {

        if (f instanceof VideoFormat) {

            final VideoFormat vf = (VideoFormat)f;

            if (BufferedImage.class.isAssignableFrom(vf.getDataClass())) {

                return super.setInputFormat(new VideoFormat("image", vf.getDataClass(), vf.getWidth(), vf.getHeight(), 8));

            }

        }

        return super.setInputFormat(null);

    }

    

    @Override

    public Format setOutputFormat(final Format f) {

        if (f instanceof VideoFormat) {

            final VideoFormat vf = (VideoFormat)f;

            return super.setOutputFormat(new VideoFormat("RLE ", byte[].class, vf.getWidth(), vf.getHeight(), 8));

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

        ByteArrayImageOutputStream tmp;

        if (out.data instanceof byte[]) {

            tmp = new ByteArrayImageOutputStream((byte[])out.data);

        }

        else {

            tmp = new ByteArrayImageOutputStream();

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

        final int offset = r.x + r.y * scanlineStride;

        try {

            final byte[] pixels = this.getIndexed8(in);

            if (pixels == null) {

                throw new UnsupportedOperationException("Can not process buffer " + in);

            }

            if ((in.flags & 0x10) != 0x0 || this.previousPixels == null) {

                this.writeKey8(tmp, pixels, r.width, r.height, offset, scanlineStride);

                out.flags = 16;

            }

            else {

                this.writeDelta8(tmp, pixels, this.previousPixels, r.width, r.height, offset, scanlineStride);

                out.flags = 0;

            }

            out.data = tmp.getBuffer();

            out.offset = 0;

            out.length = (int)tmp.getStreamPosition();

            if (this.previousPixels == null) {

                this.previousPixels = pixels.clone();

            }

            else {

                System.arraycopy(pixels, 0, this.previousPixels, 0, pixels.length);

            }

        }

        catch (IOException ex) {

            ex.printStackTrace();

            out.flags = 2;

        }

    }

    

    public void writeKey8(final OutputStream out, final byte[] data, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        final ByteArrayImageOutputStream buf = new ByteArrayImageOutputStream(data.length);

        this.writeKey8(buf, data, width, height, offset, scanlineStride);

        buf.toOutputStream(out);

    }

    

    public void writeKey8(final ImageOutputStream out, final byte[] data, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        final int ymax = offset + height * scanlineStride;

        final int upsideDown = ymax - scanlineStride + offset;

        for (int y = offset; y < ymax; y += scanlineStride) {

            int xy = upsideDown - y;

            final int xymax = xy + width;

            int literalCount = 0;

            int repeatCount = 0;

            while (xy < xymax) {

                byte v;

                for (v = data[xy], repeatCount = 0; xy < xymax && repeatCount < 255 && data[xy] == v; ++xy, ++repeatCount) {}

                xy -= repeatCount;

                if (repeatCount < 3) {

                    if (++literalCount == 254) {

                        out.write(0);

                        out.write(literalCount);

                        out.write(data, xy - literalCount + 1, literalCount);

                        literalCount = 0;

                    }

                }

                else {

                    if (literalCount > 0) {

                        if (literalCount < 3) {

                            while (literalCount > 0) {

                                out.write(1);

                                out.write(data[xy - literalCount]);

                                --literalCount;

                            }

                        }

                        else {

                            out.write(0);

                            out.write(literalCount);

                            out.write(data, xy - literalCount, literalCount);

                            if (literalCount % 2 == 1) {

                                out.write(0);

                            }

                            literalCount = 0;

                        }

                    }

                    out.write(repeatCount);

                    out.write(v);

                    xy += repeatCount - 1;

                }

                ++xy;

            }

            if (literalCount > 0) {

                if (literalCount < 3) {

                    while (literalCount > 0) {

                        out.write(1);

                        out.write(data[xy - literalCount]);

                        --literalCount;

                    }

                }

                else {

                    out.write(0);

                    out.write(literalCount);

                    out.write(data, xy - literalCount, literalCount);

                    if (literalCount % 2 == 1) {

                        out.write(0);

                    }

                }

                literalCount = 0;

            }

            out.write(0);

            out.write(0);

        }

        out.write(0);

        out.write(1);

    }

    

    public void writeDelta8(final OutputStream out, final byte[] data, final byte[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        final ByteArrayImageOutputStream buf = new ByteArrayImageOutputStream(data.length);

        this.writeDelta8(buf, data, prev, width, height, offset, scanlineStride);

        buf.toOutputStream(out);

    }

    

    public void writeDelta8(final ImageOutputStream out, final byte[] data, final byte[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        final int ymax = offset + height * scanlineStride;

        final int upsideDown = ymax - scanlineStride + offset;

        int verticalOffset = 0;

        for (int y = offset; y < ymax; y += scanlineStride) {

            int xy;

            int xymax;

            int skipCount;

            for (xy = upsideDown - y, xymax = xy + width, skipCount = 0; xy < xymax && data[xy] == prev[xy]; ++xy, ++skipCount) {}

            if (skipCount == width) {

                ++verticalOffset;

            }

            else {

                while (verticalOffset > 0 || skipCount > 0) {

                    if (verticalOffset == 1 && skipCount == 0) {

                        out.write(0);

                        out.write(0);

                        verticalOffset = 0;

                    }

                    else {

                        out.write(0);

                        out.write(2);

                        out.write(Math.min(255, skipCount));

                        out.write(Math.min(255, verticalOffset));

                        skipCount -= Math.min(255, skipCount);

                        verticalOffset -= Math.min(255, verticalOffset);

                    }

                }

                int literalCount = 0;

                int repeatCount = 0;

                while (xy < xymax) {

                    for (skipCount = 0; xy < xymax && data[xy] == prev[xy]; ++xy, ++skipCount) {}

                    byte v;

                    for (xy -= skipCount, v = data[xy], repeatCount = 0; xy < xymax && repeatCount < 255 && data[xy] == v; ++xy, ++repeatCount) {}

                    xy -= repeatCount;

                    if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {

                        ++literalCount;

                    }

                    else {

                        while (literalCount > 0) {

                            if (literalCount < 3) {

                                out.write(1);

                                out.write(data[xy - literalCount]);

                                --literalCount;

                            }

                            else {

                                final int literalRun = Math.min(254, literalCount);

                                out.write(0);

                                out.write(literalRun);

                                out.write(data, xy - literalCount, literalRun);

                                if (literalRun % 2 == 1) {

                                    out.write(0);

                                }

                                literalCount -= literalRun;

                            }

                        }

                        if (xy + skipCount == xymax) {

                            xy += skipCount - 1;

                        }

                        else if (skipCount >= repeatCount) {

                            while (skipCount > 0) {

                                out.write(0);

                                out.write(2);

                                out.write(Math.min(255, skipCount));

                                out.write(0);

                                xy += Math.min(255, skipCount);

                                skipCount -= Math.min(255, skipCount);

                            }

                            --xy;

                        }

                        else {

                            out.write(repeatCount);

                            out.write(v);

                            xy += repeatCount - 1;

                        }

                    }

                    ++xy;

                }

                while (literalCount > 0) {

                    if (literalCount < 3) {

                        out.write(1);

                        out.write(data[xy - literalCount]);

                        --literalCount;

                    }

                    else {

                        final int literalRun2 = Math.min(254, literalCount);

                        out.write(0);

                        out.write(literalRun2);

                        out.write(data, xy - literalCount, literalRun2);

                        if (literalRun2 % 2 == 1) {

                            out.write(0);

                        }

                        literalCount -= literalRun2;

                    }

                }

                out.write(0);

                out.write(0);

            }

        }

        out.write(0);

        out.write(1);

    }

}