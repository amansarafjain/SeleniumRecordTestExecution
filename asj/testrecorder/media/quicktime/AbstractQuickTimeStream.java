package asj.testrecorder.media.quicktime;



import java.awt.image.IndexColorModel;

import asj.testrecorder.media.Buffer;
import asj.testrecorder.media.Codec;
import asj.testrecorder.media.VideoFormat;
import asj.testrecorder.media.io.ImageOutputStreamAdapter;



import java.util.Iterator;

import java.io.OutputStream;


import java.util.LinkedList;

import java.io.IOException;

import java.util.ArrayList;

import java.util.Date;

import javax.imageio.stream.ImageOutputStream;



public class AbstractQuickTimeStream

{

    protected ImageOutputStream out;

    protected long streamOffset;

    protected WideDataAtom mdatAtom;

    protected long mdatOffset;

    protected CompositeAtom moovAtom;

    protected Date creationTime;

    protected long movieTimeScale;

    protected ArrayList<Track> tracks;

    protected States state;

    protected enum States

    {

        REALIZED, 

        STARTED, 

        FINISHED, 

        CLOSED;
    }

    public AbstractQuickTimeStream() {

        super();

        this.movieTimeScale = 600L;

        this.tracks = new ArrayList<Track>();

        this.state = States.REALIZED;

    }

    

    protected long getRelativeStreamPosition() throws IOException {

        return this.out.getStreamPosition() - this.streamOffset;

    }

    

    protected void seekRelative(final long newPosition) throws IOException {

        this.out.seek(newPosition + this.streamOffset);

    }

    

    protected enum MediaType

    {

        VIDEO, 

        AUDIO;

    }
    

    protected abstract class Atom

    {

        protected String type;

        protected long offset;

        

        public Atom(final String type) throws IOException {

            super();

            this.type = type;

            this.offset = AbstractQuickTimeStream.this.getRelativeStreamPosition();

        }

        

        public abstract void finish() throws IOException;

        

        public abstract long size();

    }

    

    protected class CompositeAtom extends DataAtom

    {

        protected LinkedList<Atom> children;

        

        public CompositeAtom(final String type) throws IOException {

            super(type);

            this.children = new LinkedList<Atom>();

        }

        

        public void add(final Atom child) throws IOException {

            if (this.children.size() > 0) {

                this.children.getLast().finish();

            }

            this.children.add(child);

        }

        

        @Override

        public void finish() throws IOException {

            if (!this.finished) {

                if (this.size() > 4294967295L) {

                    throw new IOException("CompositeAtom \"" + this.type + "\" is too large: " + this.size());

                }

                final long pointer = AbstractQuickTimeStream.this.getRelativeStreamPosition();

                AbstractQuickTimeStream.this.seekRelative(this.offset);

                final DataAtomOutputStream headerData = new DataAtomOutputStream(new ImageOutputStreamAdapter(AbstractQuickTimeStream.this.out));

                headerData.writeInt((int)this.size());

                headerData.writeType(this.type);

                for (final Atom child : this.children) {

                    child.finish();

                }

                AbstractQuickTimeStream.this.seekRelative(pointer);

                this.finished = true;

            }

        }

        

        @Override

        public long size() {

            long length = 8L + this.data.size();

            for (final Atom child : this.children) {

                length += child.size();

            }

            return length;

        }

    }

    

    protected class DataAtom extends Atom

    {

        protected DataAtomOutputStream data;

        protected boolean finished;

        

        public DataAtom(final String name) throws IOException {

            super(name);

            AbstractQuickTimeStream.this.out.writeLong(0L);

            this.data = new DataAtomOutputStream(new ImageOutputStreamAdapter(AbstractQuickTimeStream.this.out));

        }

        

        public DataAtomOutputStream getOutputStream() {

            if (this.finished) {

                throw new IllegalStateException("DataAtom is finished");

            }

            return this.data;

        }

        

        public long getOffset() {

            return this.offset;

        }

        

        @Override

        public void finish() throws IOException {

            if (!this.finished) {

                final long sizeBefore = this.size();

                if (this.size() > 4294967295L) {

                    throw new IOException("DataAtom \"" + this.type + "\" is too large: " + this.size());

                }

                final long pointer = AbstractQuickTimeStream.this.getRelativeStreamPosition();

                AbstractQuickTimeStream.this.seekRelative(this.offset);

                final DataAtomOutputStream headerData = new DataAtomOutputStream(new ImageOutputStreamAdapter(AbstractQuickTimeStream.this.out));

                headerData.writeUInt(this.size());

                headerData.writeType(this.type);

                AbstractQuickTimeStream.this.seekRelative(pointer);

                this.finished = true;

                final long sizeAfter = this.size();

                if (sizeBefore != sizeAfter) {

                    System.err.println("size mismatch " + sizeBefore + ".." + sizeAfter);

                }

            }

        }

        

        @Override

        public long size() {

            return 8L + this.data.size();

        }

    }

    

    protected class WideDataAtom extends Atom

    {

        protected DataAtomOutputStream data;

        protected boolean finished;

        

        public WideDataAtom(final String type) throws IOException {

            super(type);

            AbstractQuickTimeStream.this.out.writeLong(0L);

            AbstractQuickTimeStream.this.out.writeLong(0L);

            this.data = new DataAtomOutputStream(new ImageOutputStreamAdapter(AbstractQuickTimeStream.this.out)) {

                @Override

                public void flush() throws IOException {

                }

            };

        }

        

        public DataAtomOutputStream getOutputStream() {

            if (this.finished) {

                throw new IllegalStateException("Atom is finished");

            }

            return this.data;

        }

        

        public long getOffset() {

            return this.offset;

        }

        

        @Override

        public void finish() throws IOException {

            if (!this.finished) {

                final long pointer = AbstractQuickTimeStream.this.getRelativeStreamPosition();

                AbstractQuickTimeStream.this.seekRelative(this.offset);

                final DataAtomOutputStream headerData = new DataAtomOutputStream(new ImageOutputStreamAdapter(AbstractQuickTimeStream.this.out));

                final long finishedSize = this.size();

                if (finishedSize <= 4294967295L) {

                    headerData.writeUInt(8L);

                    headerData.writeType("wide");

                    headerData.writeUInt(finishedSize - 8L);

                    headerData.writeType(this.type);

                }

                else {

                    headerData.writeInt(1);

                    headerData.writeType(this.type);

                    headerData.writeLong(finishedSize - 8L);

                }

                AbstractQuickTimeStream.this.seekRelative(pointer);

                this.finished = true;

            }

        }

        

        @Override

        public long size() {

            return 16L + this.data.size();

        }

    }

    

    protected abstract static class Group

    {

        protected Sample firstSample;

        protected Sample lastSample;

        protected long sampleCount;

        protected static final long maxSampleCount = 2147483647L;

        

        protected Group(final Sample firstSample) {

            super();

            this.lastSample = firstSample;

            this.firstSample = firstSample;

            this.sampleCount = 1L;

        }

        

        protected Group(final Sample firstSample, final Sample lastSample, final long sampleCount) {

            super();

            this.firstSample = firstSample;

            this.lastSample = lastSample;

            this.sampleCount = sampleCount;

            if (sampleCount > 2147483647L) {

                throw new IllegalArgumentException("Capacity exceeded");

            }

        }

        

        protected Group(final Group group) {

            super();

            this.firstSample = group.firstSample;

            this.lastSample = group.lastSample;

            this.sampleCount = group.sampleCount;

        }

        

        protected boolean maybeAddSample(final Sample sample) {

            if (this.sampleCount < 2147483647L) {

                this.lastSample = sample;

                ++this.sampleCount;

                return true;

            }

            return false;

        }

        

        protected boolean maybeAddChunk(final Chunk chunk) {

            if (this.sampleCount + chunk.sampleCount <= 2147483647L) {

                this.lastSample = chunk.lastSample;

                this.sampleCount += chunk.sampleCount;

                return true;

            }

            return false;

        }

        

        public long getSampleCount() {

            return this.sampleCount;

        }

    }

    

    protected static class Sample

    {

        long offset;

        long length;

        long duration;

        

        public Sample(final long duration, final long offset, final long length) {

            super();

            this.duration = duration;

            this.offset = offset;

            this.length = length;

        }

    }

    

    protected static class TimeToSampleGroup extends Group

    {

        public TimeToSampleGroup(final Sample firstSample) {

            super(firstSample);

        }

        

        public TimeToSampleGroup(final Group group) {

            super(group);

        }

        

        public boolean maybeAddSample(final Sample sample) {

            return this.firstSample.duration == sample.duration && super.maybeAddSample(sample);

        }

        

        public boolean maybeAddChunk(final Chunk chunk) {

            return this.firstSample.duration == chunk.firstSample.duration && super.maybeAddChunk(chunk);

        }

        

        public long getSampleDuration() {

            return this.firstSample.duration;

        }

    }

    

    protected static class SampleSizeGroup extends Group

    {

        public SampleSizeGroup(final Sample firstSample) {

            super(firstSample);

        }

        

        public SampleSizeGroup(final Group group) {

            super(group);

        }

        

        public boolean maybeAddSample(final Sample sample) {

            return this.firstSample.length == sample.length && super.maybeAddSample(sample);

        }

        

        public boolean maybeAddChunk(final Chunk chunk) {

            return this.firstSample.length == chunk.firstSample.length && super.maybeAddChunk(chunk);

        }

        

        public long getSampleLength() {

            return this.firstSample.length;

        }

    }

    

    protected static class Chunk extends Group

    {

        protected int sampleDescriptionId;

        

        public Chunk(final Sample firstSample, final int sampleDescriptionId) {

            super(firstSample);

            this.sampleDescriptionId = sampleDescriptionId;

        }

        

        public Chunk(final Sample firstSample, final Sample lastSample, final int sampleCount, final int sampleDescriptionId) {

            super(firstSample, lastSample, sampleCount);

            this.sampleDescriptionId = sampleDescriptionId;

        }

        

        public boolean maybeAddSample(final Sample sample, final int sampleDescriptionId) {

            return sampleDescriptionId == this.sampleDescriptionId && this.lastSample.offset + this.lastSample.length == sample.offset && super.maybeAddSample(sample);

        }

        

        public boolean maybeAddChunk(final Chunk chunk) {

            return this.sampleDescriptionId == chunk.sampleDescriptionId && this.lastSample.offset + this.lastSample.length == chunk.firstSample.offset && super.maybeAddChunk(chunk);

        }

        

        public long getChunkOffset() {

            return this.firstSample.offset;

        }

    }

    

    protected abstract class Track

    {

        protected final MediaType mediaType;

        protected long mediaTimeScale;

        protected String mediaCompressionType;

        protected String mediaCompressorName;

        protected ArrayList<Chunk> chunks;

        protected ArrayList<TimeToSampleGroup> timeToSamples;

        protected ArrayList<SampleSizeGroup> sampleSizes;

        protected ArrayList<Long> syncSamples;

        protected long sampleCount;

        protected long mediaDuration;

        protected Edit[] editList;

        protected int syncInterval;

        protected Codec codec;

        protected Buffer outputBuffer;

        

        public Track(final MediaType mediaType) {

            super();

            this.mediaTimeScale = 600L;

            this.chunks = new ArrayList<Chunk>();

            this.timeToSamples = new ArrayList<TimeToSampleGroup>();

            this.sampleSizes = new ArrayList<SampleSizeGroup>();

            this.syncSamples = null;

            this.sampleCount = 0L;

            this.mediaDuration = 0L;

            this.mediaType = mediaType;

        }

        

        public void addSample(final Sample sample, final int sampleDescriptionId, final boolean isSyncSample) {

            this.mediaDuration += sample.duration;

            ++this.sampleCount;

            if (isSyncSample) {

                if (this.syncSamples != null) {

                    this.syncSamples.add(this.sampleCount);

                }

            }

            else if (this.syncSamples == null) {

                this.syncSamples = new ArrayList<Long>();

                for (long i = 1L; i < this.sampleCount; ++i) {

                    this.syncSamples.add(i);

                }

            }

            if (this.timeToSamples.isEmpty() || !this.timeToSamples.get(this.timeToSamples.size() - 1).maybeAddSample(sample)) {

                this.timeToSamples.add(new TimeToSampleGroup(sample));

            }

            if (this.sampleSizes.isEmpty() || !this.sampleSizes.get(this.sampleSizes.size() - 1).maybeAddSample(sample)) {

                this.sampleSizes.add(new SampleSizeGroup(sample));

            }

            if (this.chunks.isEmpty() || !this.chunks.get(this.chunks.size() - 1).maybeAddSample(sample, sampleDescriptionId)) {

                this.chunks.add(new Chunk(sample, sampleDescriptionId));

            }

        }

        

        public void addChunk(final Chunk chunk, final boolean isSyncSample) {

            this.mediaDuration += chunk.firstSample.duration * chunk.sampleCount;

            this.sampleCount += chunk.sampleCount;

            if (this.timeToSamples.isEmpty() || !this.timeToSamples.get(this.timeToSamples.size() - 1).maybeAddChunk(chunk)) {

                this.timeToSamples.add(new TimeToSampleGroup(chunk));

            }

            if (this.sampleSizes.isEmpty() || !this.sampleSizes.get(this.sampleSizes.size() - 1).maybeAddChunk(chunk)) {

                this.sampleSizes.add(new SampleSizeGroup(chunk));

            }

            if (this.chunks.isEmpty() || !this.chunks.get(this.chunks.size() - 1).maybeAddChunk(chunk)) {

                this.chunks.add(chunk);

            }

        }

        

        public boolean isEmpty() {

            return this.sampleCount == 0L;

        }

        

        public long getSampleCount() {

            return this.sampleCount;

        }

        

        public long getTrackDuration(final long movieTimeScale) {

            if (this.editList == null || this.editList.length == 0) {

                return this.mediaDuration * movieTimeScale / this.mediaTimeScale;

            }

            long duration = 0L;

            for (int i = 0; i < this.editList.length; ++i) {

                duration += this.editList[i].trackDuration;

            }

            return duration;

        }

        

        protected void writeTrackAtoms(final int trackIndex, final CompositeAtom moovAtom, final Date modificationTime) throws IOException {

            final CompositeAtom trakAtom = new CompositeAtom("trak");

            moovAtom.add(trakAtom);

            DataAtom leaf = new DataAtom("tkhd");

            trakAtom.add(leaf);

            DataAtomOutputStream d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(15);

            d.writeMacTimestamp(AbstractQuickTimeStream.this.creationTime);

            d.writeMacTimestamp(modificationTime);

            d.writeInt(trackIndex + 1);

            d.writeInt(0);

            d.writeUInt(this.getTrackDuration(AbstractQuickTimeStream.this.movieTimeScale));

            d.writeLong(0L);

            d.writeShort(0);

            d.writeShort(0);

            d.writeFixed8D8((this.mediaType == MediaType.AUDIO) ? 1 : 0);

            d.writeShort(0);

            d.writeFixed16D16(1.0);

            d.writeFixed16D16(0.0);

            d.writeFixed2D30(0.0);

            d.writeFixed16D16(0.0);

            d.writeFixed16D16(1.0);

            d.writeFixed2D30(0.0);

            d.writeFixed16D16(0.0);

            d.writeFixed16D16(0.0);

            d.writeFixed2D30(1.0);

            d.writeFixed16D16((this.mediaType == MediaType.VIDEO) ? ((VideoTrack)this).videoWidth : 0);

            d.writeFixed16D16((this.mediaType == MediaType.VIDEO) ? ((VideoTrack)this).videoHeight : 0);

            final CompositeAtom edtsAtom = new CompositeAtom("edts");

            trakAtom.add(edtsAtom);

            leaf = new DataAtom("elst");

            edtsAtom.add(leaf);

            d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            final Edit[] elist = this.editList;

            if (elist == null || elist.length == 0) {

                d.writeUInt(1L);

                d.writeUInt(this.getTrackDuration(AbstractQuickTimeStream.this.movieTimeScale));

                d.writeUInt(0L);

                d.writeFixed16D16(1.0);

            }

            else {

                d.writeUInt(elist.length);

                for (int i = 0; i < elist.length; ++i) {

                    d.writeUInt(elist[i].trackDuration);

                    d.writeUInt(elist[i].mediaTime);

                    d.writeUInt(elist[i].mediaRate);

                }

            }

            final CompositeAtom mdiaAtom = new CompositeAtom("mdia");

            trakAtom.add(mdiaAtom);

            leaf = new DataAtom("mdhd");

            mdiaAtom.add(leaf);

            d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            d.writeMacTimestamp(AbstractQuickTimeStream.this.creationTime);

            d.writeMacTimestamp(modificationTime);

            d.writeUInt(this.mediaTimeScale);

            d.writeUInt(this.mediaDuration);

            d.writeShort(0);

            d.writeShort(0);

            leaf = new DataAtom("hdlr");

            mdiaAtom.add(leaf);

            d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            d.writeType("mhlr");

            d.writeType((this.mediaType == MediaType.VIDEO) ? "vide" : "soun");

            if (this.mediaType == MediaType.AUDIO) {

                d.writeType("appl");

            }

            else {

                d.writeUInt(0L);

            }

            d.writeUInt((this.mediaType == MediaType.AUDIO) ? 268435456L : 0L);

            d.writeUInt((this.mediaType == MediaType.AUDIO) ? 65941 : 0);

            d.writePString((this.mediaType == MediaType.AUDIO) ? "Apple Sound Media Handler" : "");

            this.writeMediaInformationAtoms(mdiaAtom);

        }

        

        protected void writeMediaInformationAtoms(final CompositeAtom mdiaAtom) throws IOException {

            final CompositeAtom minfAtom = new CompositeAtom("minf");

            mdiaAtom.add(minfAtom);

            this.writeMediaInformationHeaderAtom(minfAtom);

            DataAtom leaf = new DataAtom("hdlr");

            minfAtom.add(leaf);

            DataAtomOutputStream d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            d.writeType("dhlr");

            d.writeType("alis");

            if (this.mediaType == MediaType.AUDIO) {

                d.writeType("appl");

            }

            else {

                d.writeUInt(0L);

            }

            d.writeUInt((this.mediaType == MediaType.AUDIO) ? 268435457L : 0L);

            d.writeInt((this.mediaType == MediaType.AUDIO) ? 65967 : 0);

            d.writePString("Apple Alias Data Handler");

            final CompositeAtom dinfAtom = new CompositeAtom("dinf");

            minfAtom.add(dinfAtom);

            leaf = new DataAtom("dref");

            dinfAtom.add(leaf);

            d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            d.writeInt(1);

            d.writeInt(12);

            d.writeType("alis");

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(1);

            this.writeSampleTableAtoms(minfAtom);

        }

        

        protected abstract void writeMediaInformationHeaderAtom(final CompositeAtom p0) throws IOException;

        

        protected abstract void writeSampleDescriptionAtom(final CompositeAtom p0) throws IOException;

        

        protected void writeSampleTableAtoms(final CompositeAtom minfAtom) throws IOException {

            final CompositeAtom stblAtom = new CompositeAtom("stbl");

            minfAtom.add(stblAtom);

            this.writeSampleDescriptionAtom(stblAtom);

            DataAtom leaf = new DataAtom("stts");

            stblAtom.add(leaf);

            DataAtomOutputStream d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            d.writeUInt(this.timeToSamples.size());

            for (final TimeToSampleGroup tts : this.timeToSamples) {

                d.writeUInt(tts.getSampleCount());

                d.writeUInt(tts.getSampleDuration());

            }

            leaf = new DataAtom("stsc");

            stblAtom.add(leaf);

            d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            int entryCount = 0;

            long previousSampleCount = -1L;

            long previousSampleDescriptionId = -1L;

            for (final Chunk c : this.chunks) {

                if (c.sampleCount != previousSampleCount || c.sampleDescriptionId != previousSampleDescriptionId) {

                    previousSampleCount = c.sampleCount;

                    previousSampleDescriptionId = c.sampleDescriptionId;

                    ++entryCount;

                }

            }

            d.writeInt(entryCount);

            int firstChunk = 1;

            previousSampleCount = -1L;

            previousSampleDescriptionId = -1L;

            for (final Chunk c2 : this.chunks) {

                if (c2.sampleCount != previousSampleCount || c2.sampleDescriptionId != previousSampleDescriptionId) {

                    previousSampleCount = c2.sampleCount;

                    previousSampleDescriptionId = c2.sampleDescriptionId;

                    d.writeUInt(firstChunk);

                    d.writeUInt(c2.sampleCount);

                    d.writeInt(c2.sampleDescriptionId);

                }

                ++firstChunk;

            }

            if (this.syncSamples != null) {

                leaf = new DataAtom("stss");

                stblAtom.add(leaf);

                d = leaf.getOutputStream();

                d.write(0);

                d.write(0);

                d.write(0);

                d.write(0);

                d.writeUInt(this.syncSamples.size());

                for (final Long number : this.syncSamples) {

                    d.writeUInt(number);

                }

            }

            leaf = new DataAtom("stsz");

            stblAtom.add(leaf);

            d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            final int sampleUnit = (this.mediaType == MediaType.AUDIO && ((AudioTrack)this).soundCompressionId != -2) ? (((AudioTrack)this).soundSampleSize / 8 * ((AudioTrack)this).soundNumberOfChannels) : 1;

            if (this.sampleSizes.size() == 1) {

                d.writeUInt(this.sampleSizes.get(0).getSampleLength() / sampleUnit);

                d.writeUInt(this.sampleSizes.get(0).getSampleCount());

            }

            else {

                d.writeUInt(0L);

                long count = 0L;

                for (final SampleSizeGroup s : this.sampleSizes) {

                    count += s.sampleCount;

                }

                d.writeUInt(count);

                for (final SampleSizeGroup s : this.sampleSizes) {

                    final long sampleSize = s.getSampleLength() / sampleUnit;

                    for (int i = 0; i < s.sampleCount; ++i) {

                        d.writeUInt(sampleSize);

                    }

                }

            }

            if (this.chunks.isEmpty() || this.chunks.get(this.chunks.size() - 1).getChunkOffset() <= 4294967295L) {

                leaf = new DataAtom("stco");

                stblAtom.add(leaf);

                d = leaf.getOutputStream();

                d.write(0);

                d.write(0);

                d.write(0);

                d.write(0);

                d.writeUInt(this.chunks.size());

                for (final Chunk c3 : this.chunks) {

                    d.writeUInt(c3.getChunkOffset() + AbstractQuickTimeStream.this.mdatOffset);

                }

            }

            else {

                leaf = new DataAtom("co64");

                stblAtom.add(leaf);

                d = leaf.getOutputStream();

                d.write(0);

                d.write(0);

                d.write(0);

                d.write(0);

                d.writeUInt(this.chunks.size());

                for (final Chunk c3 : this.chunks) {

                    d.writeLong(c3.getChunkOffset());

                }

            }

        }

    }

    

    protected class VideoTrack extends Track

    {

        protected VideoFormat videoFormat;

        protected float videoQuality;

        protected int videoWidth;

        protected int videoHeight;

        protected int videoDepth;

        protected IndexColorModel videoColorTable;

        

        public VideoTrack() {

            super(MediaType.VIDEO);

            this.videoQuality = 0.97f;

            this.videoWidth = -1;

            this.videoHeight = -1;

            this.videoDepth = -1;

        }

        

        @Override

        protected void writeMediaInformationHeaderAtom(final CompositeAtom minfAtom) throws IOException {

            final DataAtom leaf = new DataAtom("vmhd");

            minfAtom.add(leaf);

            final DataAtomOutputStream d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(1);

            d.writeShort(64);

            d.writeUShort(0);

            d.writeUShort(0);

            d.writeUShort(0);

        }

        

        @Override

        protected void writeSampleDescriptionAtom(final CompositeAtom stblAtom) throws IOException {

            final CompositeAtom leaf = new CompositeAtom("stsd");

            stblAtom.add(leaf);

            final DataAtomOutputStream d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            d.writeInt(1);

            d.writeInt(86);

            d.writeType(this.mediaCompressionType);

            d.write(new byte[6]);

            d.writeShort(1);

            d.writeShort(0);

            d.writeShort(0);

            d.writeType("java");

            d.writeInt(0);

            d.writeInt(512);

            d.writeUShort(this.videoWidth);

            d.writeUShort(this.videoHeight);

            d.writeFixed16D16(72.0);

            d.writeFixed16D16(72.0);

            d.writeInt(0);

            d.writeShort(1);

            d.writePString(this.mediaCompressorName, 32);

            d.writeShort(this.videoDepth);

            d.writeShort((this.videoColorTable == null) ? -1 : 0);

            if (this.videoColorTable != null) {

                this.writeColorTableAtom(leaf);

            }

        }

        

        protected void writeColorTableAtom(final CompositeAtom stblAtom) throws IOException {

            final DataAtom leaf = new DataAtom("ctab");

            stblAtom.add(leaf);

            final DataAtomOutputStream d = leaf.getOutputStream();

            d.writeUInt(0L);

            d.writeUShort(32768);

            d.writeUShort(this.videoColorTable.getMapSize() - 1);

            for (int i = 0, n = this.videoColorTable.getMapSize(); i < n; ++i) {

                d.writeUShort(0);

                d.writeUShort(this.videoColorTable.getRed(i) << 8 | this.videoColorTable.getRed(i));

                d.writeUShort(this.videoColorTable.getGreen(i) << 8 | this.videoColorTable.getGreen(i));

                d.writeUShort(this.videoColorTable.getBlue(i) << 8 | this.videoColorTable.getBlue(i));

            }

        }

    }

    

    protected class AudioTrack extends Track

    {

        protected int soundNumberOfChannels;

        protected int soundSampleSize;

        protected int soundCompressionId;

        protected long soundSamplesPerPacket;

        protected int soundBytesPerPacket;

        protected int soundBytesPerFrame;

        protected int soundBytesPerSample;

        protected double soundSampleRate;

        protected byte[] stsdExtensions;

        

        public AudioTrack() {

            super(MediaType.AUDIO);

            this.stsdExtensions = new byte[0];

        }

        

        @Override

        protected void writeMediaInformationHeaderAtom(final CompositeAtom minfAtom) throws IOException {

            final DataAtom leaf = new DataAtom("smhd");

            minfAtom.add(leaf);

            final DataAtomOutputStream d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            d.writeFixed8D8(0.0f);

            d.writeUShort(0);

        }

        

        @Override

        protected void writeSampleDescriptionAtom(final CompositeAtom stblAtom) throws IOException {

            final DataAtom leaf = new DataAtom("stsd");

            stblAtom.add(leaf);

            final DataAtomOutputStream d = leaf.getOutputStream();

            d.write(0);

            d.write(0);

            d.write(0);

            d.write(0);

            d.writeInt(1);

            d.writeUInt(52 + this.stsdExtensions.length);

            d.writeType(this.mediaCompressionType);

            d.write(new byte[6]);

            d.writeUShort(1);

            d.writeUShort(1);

            d.writeUShort(0);

            d.writeUInt(0L);

            d.writeUShort(this.soundNumberOfChannels);

            d.writeUShort(this.soundSampleSize);

            d.writeUShort(this.soundCompressionId);

            d.writeUShort(0);

            d.writeFixed16D16(this.soundSampleRate);

            d.writeUInt(this.soundSamplesPerPacket);

            d.writeUInt(this.soundBytesPerPacket);

            d.writeUInt(this.soundBytesPerFrame);

            d.writeUInt(this.soundBytesPerSample);

            d.write(this.stsdExtensions);

        }

    }

    

    public static class Edit

    {

        public int trackDuration;

        public int mediaTime;

        public int mediaRate;

        

        public Edit(final int trackDuration, final int mediaTime, final double mediaRate) {

            super();

            if (trackDuration < 0) {

                throw new IllegalArgumentException("trackDuration must not be < 0:" + trackDuration);

            }

            if (mediaTime < -1) {

                throw new IllegalArgumentException("mediaTime must not be < -1:" + mediaTime);

            }

            if (mediaRate <= 0.0) {

                throw new IllegalArgumentException("mediaRate must not be <= 0:" + mediaRate);

            }

            this.trackDuration = trackDuration;

            this.mediaTime = mediaTime;

            this.mediaRate = (int)(mediaRate * 65536.0);

        }

        

        public Edit(final int trackDuration, final int mediaTime, final int mediaRate) {

            super();

            if (trackDuration < 0) {

                throw new IllegalArgumentException("trackDuration must not be < 0:" + trackDuration);

            }

            if (mediaTime < -1) {

                throw new IllegalArgumentException("mediaTime must not be < -1:" + mediaTime);

            }

            if (mediaRate <= 0) {

                throw new IllegalArgumentException("mediaRate must not be <= 0:" + mediaRate);

            }

            this.trackDuration = trackDuration;

            this.mediaTime = mediaTime;

            this.mediaRate = mediaRate;

        }

    }

}