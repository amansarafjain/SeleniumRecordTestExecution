package asj.testrecorder.media.avi;



import java.util.Arrays;

import java.io.ByteArrayOutputStream;

import javax.imageio.stream.ImageOutputStream;

import java.util.zip.DeflaterOutputStream;

import java.awt.image.WritableRaster;

import java.io.IOException;

import java.io.OutputStream;

import java.awt.Rectangle;

import asj.testrecorder.media.AbstractVideoCodec;
import asj.testrecorder.media.Buffer;
import asj.testrecorder.media.Format;
import asj.testrecorder.media.VideoFormat;
import asj.testrecorder.media.io.ByteArrayImageOutputStream;
import asj.testrecorder.media.io.SeekableByteArrayOutputStream;


import java.awt.image.BufferedImage;



import java.nio.ByteOrder;





public class TechSmithCodec extends AbstractVideoCodec

{

    private ByteArrayImageOutputStream temp;

    private Object previousPixels;

    

    public TechSmithCodec() {

        super();

        this.temp = new ByteArrayImageOutputStream(ByteOrder.LITTLE_ENDIAN);

    }

    

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

            int depth = vf.getDepth();

            if (depth <= 8) {

                depth = 8;

            }

            else if (depth <= 16) {

                depth = 16;

            }

            else {

                depth = 24;

            }

            return super.setOutputFormat(new VideoFormat("tscc", byte[].class, vf.getWidth(), vf.getHeight(), depth));

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

        final int offset = r.x + r.y * scanlineStride;

        try {

            switch (vf.getDepth()) {

                case 8: {

                    final byte[] pixels = this.getIndexed8(in);

                    if (pixels == null) {

                        out.flags = 2;

                        return;

                    }

                    if ((in.flags & 0x10) != 0x0 || this.previousPixels == null) {

                        this.writeKey8(tmp, pixels, vf.getWidth(), vf.getHeight(), offset, scanlineStride);

                        out.flags = 16;

                    }

                    else {

                        this.writeDelta8(tmp, pixels, (byte[])this.previousPixels, vf.getWidth(), vf.getHeight(), offset, scanlineStride);

                        out.flags = 0;

                    }

                    if (this.previousPixels == null) {

                        this.previousPixels = pixels.clone();

                        break;

                    }

                    System.arraycopy(pixels, 0, this.previousPixels, 0, pixels.length);

                    break;

                }

                case 16: {

                    final short[] pixels2 = this.getRGB15(in);

                    if (pixels2 == null) {

                        out.flags = 2;

                        return;

                    }

                    if ((in.flags & 0x10) != 0x0 || this.previousPixels == null) {

                        this.writeKey16(tmp, pixels2, vf.getWidth(), vf.getHeight(), offset, scanlineStride);

                        out.flags = 16;

                    }

                    else {

                        this.writeDelta16(tmp, pixels2, (short[])this.previousPixels, vf.getWidth(), vf.getHeight(), offset, scanlineStride);

                        out.flags = 0;

                    }

                    if (this.previousPixels == null) {

                        this.previousPixels = pixels2.clone();

                        break;

                    }

                    System.arraycopy(pixels2, 0, this.previousPixels, 0, pixels2.length);

                    break;

                }

                case 24: {

                    final int[] pixels3 = this.getRGB24(in);

                    if (pixels3 == null) {

                        out.flags = 2;

                        return;

                    }

                    if ((in.flags & 0x10) != 0x0 || this.previousPixels == null) {

                        this.writeKey24(tmp, pixels3, vf.getWidth(), vf.getHeight(), offset, scanlineStride);

                        out.flags = 16;

                    }

                    else {

                        this.writeDelta24(tmp, pixels3, (int[])this.previousPixels, vf.getWidth(), vf.getHeight(), offset, scanlineStride);

                        out.flags = 0;

                    }

                    if (this.previousPixels == null) {

                        this.previousPixels = pixels3.clone();

                        break;

                    }

                    System.arraycopy(pixels3, 0, this.previousPixels, 0, pixels3.length);

                    break;

                }

                default: {

                    out.flags = 2;

                    return;

                }

            }

            out.data = tmp.getBuffer();

            out.offset = 0;

            out.length = (int)tmp.getStreamPosition();

        }

        catch (IOException ex) {

            ex.printStackTrace();

            out.flags = 2;

        }

    }

    

    public void writeKey8(final OutputStream out, final byte[] data, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        this.temp.clear();

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

                        this.temp.write(0);

                        this.temp.write(literalCount);

                        this.temp.write(data, xy - literalCount + 1, literalCount);

                        literalCount = 0;

                    }

                }

                else {

                    if (literalCount > 0) {

                        if (literalCount < 3) {

                            while (literalCount > 0) {

                                this.temp.write(1);

                                this.temp.write(data[xy - literalCount]);

                                --literalCount;

                            }

                        }

                        else {

                            this.temp.write(0);

                            this.temp.write(literalCount);

                            this.temp.write(data, xy - literalCount, literalCount);

                            if (literalCount % 2 == 1) {

                                this.temp.write(0);

                            }

                            literalCount = 0;

                        }

                    }

                    this.temp.write(repeatCount);

                    this.temp.write(v);

                    xy += repeatCount - 1;

                }

                ++xy;

            }

            if (literalCount > 0) {

                if (literalCount < 3) {

                    while (literalCount > 0) {

                        this.temp.write(1);

                        this.temp.write(data[xy - literalCount]);

                        --literalCount;

                    }

                }

                else {

                    this.temp.write(0);

                    this.temp.write(literalCount);

                    this.temp.write(data, xy - literalCount, literalCount);

                    if (literalCount % 2 == 1) {

                        this.temp.write(0);

                    }

                }

                literalCount = 0;

            }

            this.temp.write(0);

            this.temp.write(0);

        }

        this.temp.write(0);

        this.temp.write(1);

        final DeflaterOutputStream defl = new DeflaterOutputStream(out);

        this.temp.toOutputStream(defl);

        defl.finish();

    }

    

    public void writeDelta8(final OutputStream out, final byte[] data, final byte[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        this.temp.clear();

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

                    this.temp.write(0);

                    this.temp.write(2);

                    this.temp.write(Math.min(255, skipCount));

                    this.temp.write(Math.min(255, verticalOffset));

                    skipCount -= Math.min(255, skipCount);

                    verticalOffset -= Math.min(255, verticalOffset);

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

                                this.temp.write(1);

                                this.temp.write(data[xy - literalCount]);

                                --literalCount;

                            }

                            else {

                                final int literalRun = Math.min(254, literalCount);

                                this.temp.write(0);

                                this.temp.write(literalRun);

                                this.temp.write(data, xy - literalCount, literalRun);

                                if (literalRun % 2 == 1) {

                                    this.temp.write(0);

                                }

                                literalCount -= literalRun;

                            }

                        }

                        if (xy + skipCount == xymax) {

                            xy += skipCount - 1;

                        }

                        else if (skipCount >= repeatCount) {

                            while (skipCount > 0) {

                                this.temp.write(0);

                                this.temp.write(2);

                                this.temp.write(Math.min(255, skipCount));

                                this.temp.write(0);

                                xy += Math.min(255, skipCount);

                                skipCount -= Math.min(255, skipCount);

                            }

                            --xy;

                        }

                        else {

                            this.temp.write(repeatCount);

                            this.temp.write(v);

                            xy += repeatCount - 1;

                        }

                    }

                    ++xy;

                }

                while (literalCount > 0) {

                    if (literalCount < 3) {

                        this.temp.write(1);

                        this.temp.write(data[xy - literalCount]);

                        --literalCount;

                    }

                    else {

                        final int literalRun2 = Math.min(254, literalCount);

                        this.temp.write(0);

                        this.temp.write(literalRun2);

                        this.temp.write(data, xy - literalCount, literalRun2);

                        if (literalRun2 % 2 == 1) {

                            this.temp.write(0);

                        }

                        literalCount -= literalRun2;

                    }

                }

                this.temp.write(0);

                this.temp.write(0);

            }

        }

        this.temp.write(0);

        this.temp.write(1);

        if (this.temp.length() == 2L) {

            this.temp.toOutputStream(out);

        }

        else {

            final DeflaterOutputStream defl = new DeflaterOutputStream(out);

            this.temp.toOutputStream(defl);

            defl.finish();

        }

    }

    

    public void writeKey16(final OutputStream out, final short[] data, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        this.temp.clear();

        final int ymax = offset + height * scanlineStride;

        final int upsideDown = ymax - scanlineStride + offset;

        for (int y = offset; y < ymax; y += scanlineStride) {

            int xy = upsideDown - y;

            final int xymax = xy + width;

            int literalCount = 0;

            int repeatCount = 0;

            while (xy < xymax) {

                short v;

                for (v = data[xy], repeatCount = 0; xy < xymax && repeatCount < 255 && data[xy] == v; ++xy, ++repeatCount) {}

                xy -= repeatCount;

                if (repeatCount < 3) {

                    if (++literalCount == 254) {

                        this.temp.write(0);

                        this.temp.write(literalCount);

                        this.temp.writeShorts(data, xy - literalCount + 1, literalCount);

                        literalCount = 0;

                    }

                }

                else {

                    if (literalCount > 0) {

                        if (literalCount < 3) {

                            while (literalCount > 0) {

                                this.temp.write(1);

                                this.temp.writeShort(data[xy - literalCount]);

                                --literalCount;

                            }

                        }

                        else {

                            this.temp.write(0);

                            this.temp.write(literalCount);

                            this.temp.writeShorts(data, xy - literalCount, literalCount);

                            literalCount = 0;

                        }

                    }

                    this.temp.write(repeatCount);

                    this.temp.writeShort(v);

                    xy += repeatCount - 1;

                }

                ++xy;

            }

            if (literalCount > 0) {

                if (literalCount < 3) {

                    while (literalCount > 0) {

                        this.temp.write(1);

                        this.temp.writeShort(data[xy - literalCount]);

                        --literalCount;

                    }

                }

                else {

                    this.temp.write(0);

                    this.temp.write(literalCount);

                    this.temp.writeShorts(data, xy - literalCount, literalCount);

                }

                literalCount = 0;

            }

            this.temp.write(0);

            this.temp.write(0);

        }

        this.temp.write(0);

        this.temp.write(1);

        final DeflaterOutputStream defl = new DeflaterOutputStream(out);

        this.temp.toOutputStream(defl);

        defl.finish();

    }

    

    public void writeDelta16(final OutputStream out, final short[] data, final short[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        this.temp.clear();

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

                    this.temp.write(0);

                    this.temp.write(2);

                    this.temp.write(Math.min(255, skipCount));

                    this.temp.write(Math.min(255, verticalOffset));

                    skipCount -= Math.min(255, skipCount);

                    verticalOffset -= Math.min(255, verticalOffset);

                }

                int literalCount = 0;

                int repeatCount = 0;

                while (xy < xymax) {

                    for (skipCount = 0; xy < xymax && data[xy] == prev[xy]; ++xy, ++skipCount) {}

                    short v;

                    for (xy -= skipCount, v = data[xy], repeatCount = 0; xy < xymax && repeatCount < 255 && data[xy] == v; ++xy, ++repeatCount) {}

                    xy -= repeatCount;

                    if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {

                        ++literalCount;

                    }

                    else {

                        while (literalCount > 0) {

                            if (literalCount < 3) {

                                this.temp.write(1);

                                this.temp.writeShort(data[xy - literalCount]);

                                --literalCount;

                            }

                            else {

                                final int literalRun = Math.min(254, literalCount);

                                this.temp.write(0);

                                this.temp.write(literalRun);

                                this.temp.writeShorts(data, xy - literalCount, literalRun);

                                literalCount -= literalRun;

                            }

                        }

                        if (xy + skipCount == xymax) {

                            xy += skipCount - 1;

                        }

                        else if (skipCount >= repeatCount) {

                            while (skipCount > 0) {

                                this.temp.write(0);

                                this.temp.write(2);

                                this.temp.write(Math.min(255, skipCount));

                                this.temp.write(0);

                                xy += Math.min(255, skipCount);

                                skipCount -= Math.min(255, skipCount);

                            }

                            --xy;

                        }

                        else {

                            this.temp.write(repeatCount);

                            this.temp.writeShort(v);

                            xy += repeatCount - 1;

                        }

                    }

                    ++xy;

                }

                while (literalCount > 0) {

                    if (literalCount < 3) {

                        this.temp.write(1);

                        this.temp.writeShort(data[xy - literalCount]);

                        --literalCount;

                    }

                    else {

                        final int literalRun2 = Math.min(254, literalCount);

                        this.temp.write(0);

                        this.temp.write(literalRun2);

                        this.temp.writeShorts(data, xy - literalCount, literalRun2);

                        literalCount -= literalRun2;

                    }

                }

                this.temp.write(0);

                this.temp.write(0);

            }

        }

        this.temp.write(0);

        this.temp.write(1);

        if (this.temp.length() == 2L) {

            this.temp.toOutputStream(out);

        }

        else {

            final DeflaterOutputStream defl = new DeflaterOutputStream(out);

            this.temp.toOutputStream(defl);

            defl.finish();

        }

    }

    

    public void writeKey24(final OutputStream out, final int[] data, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        this.temp.clear();

        final int ymax = offset + height * scanlineStride;

        final int upsideDown = ymax - scanlineStride + offset;

        for (int y = offset; y < ymax; y += scanlineStride) {

            int xy = upsideDown - y;

            final int xymax = xy + width;

            int literalCount = 0;

            int repeatCount = 0;

            while (xy < xymax) {

                int v;

                for (v = data[xy], repeatCount = 0; xy < xymax && repeatCount < 255 && data[xy] == v; ++xy, ++repeatCount) {}

                xy -= repeatCount;

                if (repeatCount < 3) {

                    if (++literalCount == 254) {

                        this.temp.write(0);

                        this.temp.write(literalCount);

                        this.writeInts24LE(this.temp, data, xy - literalCount + 1, literalCount);

                        literalCount = 0;

                    }

                }

                else {

                    if (literalCount > 0) {

                        if (literalCount < 3) {

                            while (literalCount > 0) {

                                this.temp.write(1);

                                this.writeInt24LE(this.temp, data[xy - literalCount]);

                                --literalCount;

                            }

                        }

                        else {

                            this.temp.write(0);

                            this.temp.write(literalCount);

                            this.writeInts24LE(this.temp, data, xy - literalCount, literalCount);

                            literalCount = 0;

                        }

                    }

                    this.temp.write(repeatCount);

                    this.writeInt24LE(this.temp, v);

                    xy += repeatCount - 1;

                }

                ++xy;

            }

            if (literalCount > 0) {

                if (literalCount < 3) {

                    while (literalCount > 0) {

                        this.temp.write(1);

                        this.writeInt24LE(this.temp, data[xy - literalCount]);

                        --literalCount;

                    }

                }

                else {

                    this.temp.write(0);

                    this.temp.write(literalCount);

                    this.writeInts24LE(this.temp, data, xy - literalCount, literalCount);

                }

                literalCount = 0;

            }

            this.temp.write(0);

            this.temp.write(0);

        }

        this.temp.write(0);

        this.temp.write(1);

        final DeflaterOutputStream defl = new DeflaterOutputStream(out);

        this.temp.toOutputStream(defl);

        defl.finish();

    }

    

    public void writeDelta24(final OutputStream out, final int[] data, final int[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        this.temp.clear();

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

                    this.temp.write(0);

                    this.temp.write(2);

                    this.temp.write(Math.min(255, skipCount));

                    this.temp.write(Math.min(255, verticalOffset));

                    skipCount -= Math.min(255, skipCount);

                    verticalOffset -= Math.min(255, verticalOffset);

                }

                int literalCount = 0;

                int repeatCount = 0;

                while (xy < xymax) {

                    for (skipCount = 0; xy < xymax && data[xy] == prev[xy]; ++xy, ++skipCount) {}

                    int v;

                    for (xy -= skipCount, v = data[xy], repeatCount = 0; xy < xymax && repeatCount < 255 && data[xy] == v; ++xy, ++repeatCount) {}

                    xy -= repeatCount;

                    if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {

                        ++literalCount;

                    }

                    else {

                        while (literalCount > 0) {

                            if (literalCount < 3) {

                                this.temp.write(1);

                                this.writeInt24LE(this.temp, data[xy - literalCount]);

                                --literalCount;

                            }

                            else {

                                final int literalRun = Math.min(254, literalCount);

                                this.temp.write(0);

                                this.temp.write(literalRun);

                                this.writeInts24LE(this.temp, data, xy - literalCount, literalRun);

                                literalCount -= literalRun;

                            }

                        }

                        if (xy + skipCount == xymax) {

                            xy += skipCount - 1;

                        }

                        else if (skipCount >= repeatCount) {

                            while (skipCount > 0) {

                                this.temp.write(0);

                                this.temp.write(2);

                                this.temp.write(Math.min(255, skipCount));

                                this.temp.write(0);

                                xy += Math.min(255, skipCount);

                                skipCount -= Math.min(255, skipCount);

                            }

                            --xy;

                        }

                        else {

                            this.temp.write(repeatCount);

                            this.writeInt24LE(this.temp, v);

                            xy += repeatCount - 1;

                        }

                    }

                    ++xy;

                }

                while (literalCount > 0) {

                    if (literalCount < 3) {

                        this.temp.write(1);

                        this.writeInt24LE(this.temp, data[xy - literalCount]);

                        --literalCount;

                    }

                    else {

                        final int literalRun2 = Math.min(254, literalCount);

                        this.temp.write(0);

                        this.temp.write(literalRun2);

                        this.writeInts24LE(this.temp, data, xy - literalCount, literalRun2);

                        literalCount -= literalRun2;

                    }

                }

                this.temp.write(0);

                this.temp.write(0);

            }

        }

        this.temp.write(0);

        this.temp.write(1);

        if (this.temp.length() == 2L) {

            this.temp.toOutputStream(out);

        }

        else {

            final DeflaterOutputStream defl = new DeflaterOutputStream(out);

            this.temp.toOutputStream(defl);

            defl.finish();

        }

    }

    

    public static void main(final String[] args) {

        final byte[] data = { 8, 2, 3, 4, 4, 3, 7, 7, 7, 8, 8, 1, 1, 1, 1, 2, 7, 7, 7, 8, 8, 0, 2, 0, 0, 0, 7, 7, 7, 8, 8, 2, 2, 3, 4, 4, 7, 7, 7, 8, 8, 1, 4, 4, 4, 5, 7, 7, 7, 8 };

        final byte[] prev = { 8, 3, 3, 3, 3, 3, 7, 7, 7, 8, 8, 1, 1, 1, 1, 1, 7, 7, 7, 8, 8, 5, 5, 5, 5, 0, 7, 7, 7, 8, 8, 2, 2, 0, 0, 0, 7, 7, 7, 8, 8, 2, 0, 0, 0, 5, 7, 7, 7, 8 };

        final ByteArrayOutputStream buf = new ByteArrayOutputStream();

        final DataChunkOutputStream out = new DataChunkOutputStream(buf);

        final TechSmithCodec enc = new TechSmithCodec();

        try {

            enc.writeDelta8(out, data, prev, 1, 8, 10, 5);

            out.close();

            final byte[] result = buf.toByteArray();

            System.out.println("size:" + result.length);

            System.out.println(Arrays.toString(result));

            System.out.print("0x [");

            for (int i = 0; i < result.length; ++i) {

                if (i != 0) {

                    System.out.print(',');

                }

                final String hex = "00" + Integer.toHexString(result[i]);

                System.out.print(hex.substring(hex.length() - 2));

            }

            System.out.println(']');

        }

        catch (IOException ex) {

            ex.printStackTrace();

        }

    }

}