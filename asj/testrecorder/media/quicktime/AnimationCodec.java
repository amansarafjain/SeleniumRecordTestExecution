package asj.testrecorder.media.quicktime;



import java.io.EOFException;

import javax.imageio.stream.ImageInputStream;

import java.nio.ByteOrder;

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






public class AnimationCodec extends AbstractVideoCodec

{

    private Object previousPixels;

    private short[] test;

    

    @Override

    public Format setInputFormat(final Format f) {

        if (f instanceof VideoFormat) {

            final VideoFormat vf = (VideoFormat)f;

            int depth = vf.getDepth();

            if (depth <= 8) {

                depth = 8;

            }

            else if (depth <= 16) {

                depth = 16;

            }

            else if (depth <= 24) {

                depth = 24;

            }

            else {

                depth = 32;

            }

            if (BufferedImage.class.isAssignableFrom(vf.getDataClass())) {

                return super.setInputFormat(new VideoFormat("rle ", "Animation", vf.getDataClass(), vf.getWidth(), vf.getHeight(), depth));

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

            else if (depth <= 24) {

                depth = 24;

            }

            else {

                depth = 32;

            }

            return super.setOutputFormat(new VideoFormat("rle ", "Animation", byte[].class, vf.getWidth(), vf.getHeight(), depth));

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

        try {

            switch (vf.getDepth()) {

                case 8: {

                    final byte[] pixels = this.getIndexed8(in);

                    if (pixels == null) {

                        throw new UnsupportedOperationException("Unable to process buffer " + in);

                    }

                    if ((in.flags & 0x10) != 0x0 || this.previousPixels == null) {

                        this.encodeKey8(tmp, pixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

                        out.flags = 16;

                    }

                    else {

                        this.encodeDelta8(tmp, pixels, (byte[])this.previousPixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

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

                        throw new UnsupportedOperationException("Unable to process buffer " + in);

                    }

                    if ((in.flags & 0x10) != 0x0 || this.previousPixels == null) {

                        this.encodeKey16(tmp, pixels2, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

                        out.flags = 16;

                    }

                    else {

                        this.encodeDelta16(tmp, pixels2, (short[])this.previousPixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

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

                        throw new UnsupportedOperationException("Unable to process buffer " + in);

                    }

                    if ((in.flags & 0x10) != 0x0 || this.previousPixels == null) {

                        this.encodeKey24(tmp, pixels3, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

                        out.flags = 16;

                    }

                    else {

                        this.encodeDelta24(tmp, pixels3, (int[])this.previousPixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

                        out.flags = 0;

                    }

                    if (this.previousPixels == null) {

                        this.previousPixels = pixels3.clone();

                        break;

                    }

                    System.arraycopy(pixels3, 0, this.previousPixels, 0, pixels3.length);

                    break;

                }

                case 32: {

                    final int[] pixels3 = this.getARGB32(in);

                    if (pixels3 == null) {

                        out.flags = 2;

                        return;

                    }

                    if ((in.flags & 0x10) != 0x0 || this.previousPixels == null) {

                        this.encodeKey32(tmp, pixels3, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

                        out.flags = 16;

                    }

                    else {

                        this.encodeDelta32(tmp, pixels3, (int[])this.previousPixels, r.width, r.height, r.x + r.y * scanlineStride, scanlineStride);

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

    

    public void encodeKey8(final ImageOutputStream out, final byte[] data, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        if (width % 4 != 0 || offset % 4 != 0 || scanlineStride % 4 != 0) {

            throw new UnsupportedOperationException("Conversion is not fully implemented yet.");

        }

        final int[] ints = new int[data.length / 4];

        for (int i = 0, j = 0; i < data.length; i += 4, ++j) {

            ints[j] = ((data[i] & 0xFF) << 24 | (data[i + 1] & 0xFF) << 16 | (data[i + 2] & 0xFF) << 8 | (data[i + 3] & 0xFF));

        }

        this.encodeKey32(out, ints, width / 4, height, offset / 4, scanlineStride / 4);

    }

    

    public void encodeDelta8(final ImageOutputStream out, final byte[] data, final byte[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        if (width % 4 != 0 || offset % 4 != 0 || scanlineStride % 4 != 0) {

            throw new UnsupportedOperationException("Conversion is not fully implemented yet.");

        }

        out.setByteOrder(ByteOrder.BIG_ENDIAN);

        final int[] ints = new int[data.length / 4];

        for (int i = 0, j = 0; i < data.length; i += 4, ++j) {

            ints[j] = ((data[i] & 0xFF) << 24 | (data[i + 1] & 0xFF) << 16 | (data[i + 2] & 0xFF) << 8 | (data[i + 3] & 0xFF));

        }

        final int[] pints = new int[prev.length / 4];

        for (int k = 0, l = 0; k < prev.length; k += 4, ++l) {

            pints[l] = ((prev[k] & 0xFF) << 24 | (prev[k + 1] & 0xFF) << 16 | (prev[k + 2] & 0xFF) << 8 | (prev[k + 3] & 0xFF));

        }

        this.encodeDelta32(out, ints, pints, width / 4, height, offset / 4, scanlineStride / 4);

    }

    

    public void encodeKey16(final ImageOutputStream out, final short[] data, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        out.setByteOrder(ByteOrder.BIG_ENDIAN);

        final long headerPos = out.getStreamPosition();

        out.writeInt(0);

        out.writeShort(0);

        for (int ymax = offset + height * scanlineStride, y = offset; y < ymax; y += scanlineStride) {

            int xy = y;

            final int xymax = y + width;

            out.write(1);

            int literalCount = 0;

            int repeatCount = 0;

            while (xy < xymax) {

                short v;

                for (v = data[xy], repeatCount = 0; xy < xymax && repeatCount < 127 && data[xy] == v; ++xy, ++repeatCount) {}

                xy -= repeatCount;

                if (repeatCount < 2) {

                    if (++literalCount == 127) {

                        out.write(literalCount);

                        out.writeShorts(data, xy - literalCount + 1, literalCount);

                        literalCount = 0;

                    }

                }

                else {

                    if (literalCount > 0) {

                        out.write(literalCount);

                        out.writeShorts(data, xy - literalCount, literalCount);

                        literalCount = 0;

                    }

                    out.write(-repeatCount);

                    out.writeShort(v);

                    xy += repeatCount - 1;

                }

                ++xy;

            }

            if (literalCount > 0) {

                out.write(literalCount);

                out.writeShorts(data, xy - literalCount, literalCount);

                literalCount = 0;

            }

            out.write(-1);

        }

        final long pos = out.getStreamPosition();

        out.seek(headerPos);

        out.writeInt((int)(pos - headerPos));

        out.seek(pos);

    }

    

    public void encodeDelta16(final ImageOutputStream out, final short[] data, final short[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        out.setByteOrder(ByteOrder.BIG_ENDIAN);

        int ymax = 0;

        int ymin = 0;

    Label_0078:

        for (ymax = offset + height * scanlineStride, ymin = offset; ymin < ymax; ymin += scanlineStride) {

            for (int xy = ymin, xymax = ymin + width; xy < xymax; ++xy) {

                if (data[xy] != prev[xy]) {

                    break Label_0078;

                }

            }

        }

        if (ymin == ymax) {

            out.writeInt(4);

            return;

        }

    Label_0151:

        while (ymax > ymin) {

            for (int xy = ymax - scanlineStride, xymax = ymax - scanlineStride + width; xy < xymax; ++xy) {

                if (data[xy] != prev[xy]) {

                    break Label_0151;

                }

            }

            ymax -= scanlineStride;

        }

        final long headerPos = out.getStreamPosition();

        out.writeInt(0);

        if (ymin == offset && ymax == offset + height * scanlineStride) {

            out.writeShort(0);

        }

        else {

            out.writeShort(8);

            out.writeShort((ymin - offset) / scanlineStride);

            out.writeShort(0);

            out.writeShort((ymax - ymin + 1 - offset) / scanlineStride);

            out.writeShort(0);

        }

        for (int y = ymin; y < ymax; y += scanlineStride) {

            int xy2;

            int xymax2;

            int skipCount;

            for (xy2 = y, xymax2 = y + width, skipCount = 0; xy2 < xymax2 && data[xy2] == prev[xy2]; ++xy2, ++skipCount) {}

            if (skipCount == width) {

                out.write(1);

                out.write(-1);

            }

            else {

                out.write(Math.min(255, skipCount + 1));

                for (skipCount -= Math.min(254, skipCount); skipCount > 0; skipCount -= Math.min(254, skipCount)) {

                    out.write(0);

                    out.write(Math.min(255, skipCount + 1));

                }

                int literalCount = 0;

                int repeatCount = 0;

                while (xy2 < xymax2) {

                    for (skipCount = 0; xy2 < xymax2 && data[xy2] == prev[xy2]; ++xy2, ++skipCount) {}

                    short v;

                    for (xy2 -= skipCount, v = data[xy2], repeatCount = 0; xy2 < xymax2 && repeatCount < 127 && data[xy2] == v; ++xy2, ++repeatCount) {}

                    xy2 -= repeatCount;

                    if (skipCount < 2 && xy2 + skipCount < xymax2 && repeatCount < 2) {

                        if (++literalCount == 127) {

                            out.write(literalCount);

                            out.writeShorts(data, xy2 - literalCount + 1, literalCount);

                            literalCount = 0;

                        }

                    }

                    else {

                        if (literalCount > 0) {

                            out.write(literalCount);

                            out.writeShorts(data, xy2 - literalCount, literalCount);

                            literalCount = 0;

                        }

                        if (xy2 + skipCount == xymax2) {

                            xy2 += skipCount - 1;

                        }

                        else if (skipCount >= repeatCount) {

                            xy2 += skipCount - 1;

                            while (skipCount > 0) {

                                out.write(0);

                                out.write(Math.min(255, skipCount + 1));

                                skipCount -= Math.min(254, skipCount);

                            }

                        }

                        else {

                            out.write(-repeatCount);

                            out.writeShort(v);

                            xy2 += repeatCount - 1;

                        }

                    }

                    ++xy2;

                }

                if (literalCount > 0) {

                    out.write(literalCount);

                    out.writeShorts(data, xy2 - literalCount, literalCount);

                    literalCount = 0;

                }

                out.write(-1);

            }

        }

        final long pos = out.getStreamPosition();

        out.seek(headerPos);

        out.writeInt((int)(pos - headerPos));

        out.seek(pos);

    }

    

    public void encodeKey24(final ImageOutputStream out, final int[] data, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        out.setByteOrder(ByteOrder.BIG_ENDIAN);

        final long headerPos = out.getStreamPosition();

        out.writeInt(0);

        out.writeShort(0);

        for (int ymax = offset + height * scanlineStride, y = offset; y < ymax; y += scanlineStride) {

            int xy = y;

            final int xymax = y + width;

            out.write(1);

            int literalCount = 0;

            int repeatCount = 0;

            while (xy < xymax) {

                int v;

                for (v = data[xy], repeatCount = 0; xy < xymax && repeatCount < 127 && data[xy] == v; ++xy, ++repeatCount) {}

                xy -= repeatCount;

                if (repeatCount < 2) {

                    if (++literalCount > 126) {

                        out.write(literalCount);

                        this.writeInts24(out, data, xy - literalCount + 1, literalCount);

                        literalCount = 0;

                    }

                }

                else {

                    if (literalCount > 0) {

                        out.write(literalCount);

                        this.writeInts24(out, data, xy - literalCount, literalCount);

                        literalCount = 0;

                    }

                    out.write(-repeatCount);

                    this.writeInt24(out, v);

                    xy += repeatCount - 1;

                }

                ++xy;

            }

            if (literalCount > 0) {

                out.write(literalCount);

                this.writeInts24(out, data, xy - literalCount, literalCount);

                literalCount = 0;

            }

            out.write(-1);

        }

        final long pos = out.getStreamPosition();

        out.seek(headerPos);

        out.writeInt((int)(pos - headerPos));

        out.seek(pos);

    }

    

    public void encodeDelta24(final ImageOutputStream out, final int[] data, final int[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        out.setByteOrder(ByteOrder.BIG_ENDIAN);

        int ymax = 0;

        int ymin = 0;

    Label_0078:

        for (ymax = offset + height * scanlineStride, ymin = offset; ymin < ymax; ymin += scanlineStride) {

            for (int xy = ymin, xymax = ymin + width; xy < xymax; ++xy) {

                if (data[xy] != prev[xy]) {

                    break Label_0078;

                }

            }

        }

        if (ymin == ymax) {

            out.writeInt(4);

            return;

        }

    Label_0151:

        while (ymax > ymin) {

            for (int xy = ymax - scanlineStride, xymax = ymax - scanlineStride + width; xy < xymax; ++xy) {

                if (data[xy] != prev[xy]) {

                    break Label_0151;

                }

            }

            ymax -= scanlineStride;

        }

        final long headerPos = out.getStreamPosition();

        out.writeInt(0);

        if (ymin == offset && ymax == offset + height * scanlineStride) {

            out.writeShort(0);

        }

        else {

            out.writeShort(8);

            out.writeShort((ymin - offset) / scanlineStride);

            out.writeShort(0);

            out.writeShort((ymax - ymin + 1 - offset) / scanlineStride);

            out.writeShort(0);

        }

        for (int y = ymin; y < ymax; y += scanlineStride) {

            int xy2;

            int xymax2;

            int skipCount;

            for (xy2 = y, xymax2 = y + width, skipCount = 0; xy2 < xymax2 && data[xy2] == prev[xy2]; ++xy2, ++skipCount) {}

            if (skipCount == width) {

                out.write(1);

                out.write(-1);

            }

            else {

                out.write(Math.min(255, skipCount + 1));

                for (skipCount -= Math.min(254, skipCount); skipCount > 0; skipCount -= Math.min(254, skipCount)) {

                    out.write(0);

                    out.write(Math.min(255, skipCount + 1));

                }

                int literalCount = 0;

                int repeatCount = 0;

                while (xy2 < xymax2) {

                    for (skipCount = 0; xy2 < xymax2 && data[xy2] == prev[xy2]; ++xy2, ++skipCount) {}

                    int v;

                    for (xy2 -= skipCount, v = data[xy2], repeatCount = 0; xy2 < xymax2 && repeatCount < 127 && data[xy2] == v; ++xy2, ++repeatCount) {}

                    xy2 -= repeatCount;

                    if (skipCount < 1 && xy2 + skipCount < xymax2 && repeatCount < 2) {

                        if (++literalCount == 127) {

                            out.write(literalCount);

                            this.writeInts24(out, data, xy2 - literalCount + 1, literalCount);

                            literalCount = 0;

                        }

                    }

                    else {

                        if (literalCount > 0) {

                            out.write(literalCount);

                            this.writeInts24(out, data, xy2 - literalCount, literalCount);

                            literalCount = 0;

                        }

                        if (xy2 + skipCount == xymax2) {

                            xy2 += skipCount - 1;

                        }

                        else if (skipCount >= repeatCount) {

                            xy2 += skipCount - 1;

                            while (skipCount > 0) {

                                out.write(0);

                                out.write(Math.min(255, skipCount + 1));

                                skipCount -= Math.min(254, skipCount);

                            }

                        }

                        else {

                            out.write(-repeatCount);

                            this.writeInt24(out, v);

                            xy2 += repeatCount - 1;

                        }

                    }

                    ++xy2;

                }

                if (literalCount > 0) {

                    out.write(literalCount);

                    this.writeInts24(out, data, xy2 - literalCount, literalCount);

                    literalCount = 0;

                }

                out.write(-1);

            }

        }

        final long pos = out.getStreamPosition();

        out.seek(headerPos);

        out.writeInt((int)(pos - headerPos));

        out.seek(pos);

    }

    

    public void encodeKey32(final ImageOutputStream out, final int[] data, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        out.setByteOrder(ByteOrder.BIG_ENDIAN);

        final long headerPos = out.getStreamPosition();

        out.writeInt(0);

        out.writeShort(0);

        for (int ymax = offset + height * scanlineStride, y = offset; y < ymax; y += scanlineStride) {

            int xy = y;

            final int xymax = y + width;

            out.write(1);

            int literalCount = 0;

            int repeatCount = 0;

            while (xy < xymax) {

                int v;

                for (v = data[xy], repeatCount = 0; xy < xymax && repeatCount < 127 && data[xy] == v; ++xy, ++repeatCount) {}

                xy -= repeatCount;

                if (repeatCount < 2) {

                    if (++literalCount > 126) {

                        out.write(literalCount);

                        out.writeInts(data, xy - literalCount + 1, literalCount);

                        literalCount = 0;

                    }

                }

                else {

                    if (literalCount > 0) {

                        out.write(literalCount);

                        out.writeInts(data, xy - literalCount, literalCount);

                        literalCount = 0;

                    }

                    out.write(-repeatCount);

                    out.writeInt(v);

                    xy += repeatCount - 1;

                }

                ++xy;

            }

            if (literalCount > 0) {

                out.write(literalCount);

                out.writeInts(data, xy - literalCount, literalCount);

                literalCount = 0;

            }

            out.write(-1);

        }

        final long pos = out.getStreamPosition();

        out.seek(headerPos);

        out.writeInt((int)(pos - headerPos));

        out.seek(pos);

    }

    

    public void encodeDelta32(final ImageOutputStream out, final int[] data, final int[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        out.setByteOrder(ByteOrder.BIG_ENDIAN);

        int ymax = 0;

        int ymin = 0;

    Label_0078:

        for (ymax = offset + height * scanlineStride, ymin = offset; ymin < ymax; ymin += scanlineStride) {

            for (int xy = ymin, xymax = ymin + width; xy < xymax; ++xy) {

                if (data[xy] != prev[xy]) {

                    break Label_0078;

                }

            }

        }

        if (ymin == ymax) {

            out.writeInt(4);

            return;

        }

    Label_0151:

        while (ymax > ymin) {

            for (int xy = ymax - scanlineStride, xymax = ymax - scanlineStride + width; xy < xymax; ++xy) {

                if (data[xy] != prev[xy]) {

                    break Label_0151;

                }

            }

            ymax -= scanlineStride;

        }

        final long headerPos = out.getStreamPosition();

        out.writeInt(0);

        if (ymin == offset && ymax == offset + height * scanlineStride) {

            out.writeShort(0);

        }

        else {

            out.writeShort(8);

            out.writeShort((ymin - offset) / scanlineStride);

            out.writeShort(0);

            out.writeShort((ymax - ymin + 1 - offset) / scanlineStride);

            out.writeShort(0);

        }

        for (int y = ymin; y < ymax; y += scanlineStride) {

            int xy2;

            int xymax2;

            int skipCount;

            for (xy2 = y, xymax2 = y + width, skipCount = 0; xy2 < xymax2 && data[xy2] == prev[xy2]; ++xy2, ++skipCount) {}

            if (skipCount == width) {

                out.write(1);

                out.write(-1);

            }

            else {

                out.write(Math.min(255, skipCount + 1));

                if (skipCount > 254) {

                    for (skipCount -= 254; skipCount > 254; skipCount -= 254) {

                        out.write(0);

                        out.write(255);

                    }

                    out.write(0);

                    out.write(skipCount + 1);

                }

                int literalCount = 0;

                int repeatCount = 0;

                while (xy2 < xymax2) {

                    for (skipCount = 0; xy2 < xymax2 && data[xy2] == prev[xy2]; ++xy2, ++skipCount) {}

                    int v;

                    for (xy2 -= skipCount, v = data[xy2], repeatCount = 0; xy2 < xymax2 && repeatCount < 127 && data[xy2] == v; ++xy2, ++repeatCount) {}

                    xy2 -= repeatCount;

                    if (skipCount < 1 && xy2 + skipCount < xymax2 && repeatCount < 2) {

                        if (++literalCount == 127) {

                            out.write(literalCount);

                            out.writeInts(data, xy2 - literalCount + 1, literalCount);

                            literalCount = 0;

                        }

                    }

                    else {

                        if (literalCount > 0) {

                            out.write(literalCount);

                            out.writeInts(data, xy2 - literalCount, literalCount);

                            literalCount = 0;

                        }

                        if (xy2 + skipCount == xymax2) {

                            xy2 += skipCount - 1;

                        }

                        else if (skipCount >= repeatCount) {

                            while (skipCount > 254) {

                                out.write(0);

                                out.write(255);

                                xy2 += 254;

                                skipCount -= 254;

                            }

                            out.write(0);

                            out.write(skipCount + 1);

                            xy2 += skipCount - 1;

                        }

                        else {

                            out.write(-repeatCount);

                            out.writeInt(v);

                            xy2 += repeatCount - 1;

                        }

                    }

                    ++xy2;

                }

                if (literalCount > 0) {

                    out.write(literalCount);

                    out.writeInts(data, xy2 - literalCount, literalCount);

                    literalCount = 0;

                }

                out.write(-1);

            }

        }

        final long pos = out.getStreamPosition();

        out.seek(headerPos);

        out.writeInt((int)(pos - headerPos));

        out.seek(pos);

    }

    

    public void decodeDelta16(final ImageInputStream in, final short[] data, final short[] prev, final int width, final int height, final int offset, final int scanlineStride) throws IOException {

        in.setByteOrder(ByteOrder.BIG_ENDIAN);

        final long chunkSize = in.readUnsignedInt();

        if (chunkSize <= 8L) {

            return;

        }

        if (in.length() != chunkSize) {

            throw new IOException("Illegal chunk size:" + chunkSize + " expected:" + in.length());

        }

        final int header = in.readUnsignedShort();

        int startingLine;

        int numberOfLines;

        if (header == 0) {

            startingLine = 0;

            numberOfLines = height;

        }

        else {

            if (header != 8) {

                throw new IOException("Unknown header 0x" + Integer.toHexString(header));

            }

            startingLine = in.readUnsignedShort();

            final int reserved1 = in.readUnsignedShort();

            if (reserved1 != 0) {

                throw new IOException("Illegal value in reserved1 0x" + Integer.toHexString(reserved1));

            }

            numberOfLines = in.readUnsignedShort();

            final int reserved2 = in.readUnsignedShort();

            if (reserved2 != 0) {

                throw new IOException("Illegal value in reserved2 0x" + Integer.toHexString(reserved2));

            }

        }

        if (startingLine > height || numberOfLines == 0) {

            return;

        }

        if (startingLine + numberOfLines - 1 > height) {

            throw new IOException("Illegal startingLine or numberOfLines, startingLine=" + startingLine + ", numberOfLines=" + numberOfLines);

        }

        for (int l = 0; l < numberOfLines; ++l) {

            int i = offset + (startingLine + l) * scanlineStride;

            final int skipCode = in.readUnsignedByte() - 1;

            if (skipCode == -1) {

                break;

            }

            if (skipCode > 0) {

                if (data == prev) {

                    i += skipCode;

                }

                else {

                    for (int j = 0; j < skipCode; ++j) {

                        data[i] = prev[i];

                        ++i;

                    }

                }

            }

            while (true) {

                final int opCode = in.readByte();

                if (opCode == 0) {

                    final int skipCode2 = in.readUnsignedByte() - 1;

                    if (skipCode2 <= 0) {

                        continue;

                    }

                    if (prev != data) {

                        System.arraycopy(prev, i, data, i, skipCode2);

                    }

                    i += skipCode2;

                }

                else if (opCode > 0) {

                    try {

                        in.readFully(data, i, opCode);

                    }

                    catch (EOFException e) {

                        System.exit(5);

                        return;

                    }

                    i += opCode;

                }

                else {

                    if (opCode == -1) {

                        break;

                    }

                    if (opCode >= -1) {

                        continue;

                    }

                    final short d = in.readShort();

                    for (int end = i - opCode; i < end; data[i++] = d) {}

                }

            }

            assert i <= offset + (startingLine + l + 1) * scanlineStride;

        }

        assert in.getStreamPosition() == in.length();

    }

}