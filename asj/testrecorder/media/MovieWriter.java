package asj.testrecorder.media;

import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract interface MovieWriter
{
  public abstract void writeFrame(int paramInt, BufferedImage paramBufferedImage, long paramLong)
    throws IOException;
  
  public abstract void writeSample(int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3, long paramLong, boolean paramBoolean)
    throws IOException;
  
  public abstract void writeSamples(int paramInt1, int paramInt2, byte[] paramArrayOfByte, int paramInt3, int paramInt4, long paramLong, boolean paramBoolean)
    throws IOException;
  
  public abstract void close()
    throws IOException;
  
  public abstract boolean isVFRSupported();
  
  public abstract boolean isDataLimitReached();
}


/* Location:           D:\R&D\ATUTestRecorder_2.1\ATUTestRecorder_2.1.jar
 * Qualified Name:     atu.testrecorder.media.MovieWriter
 * JD-Core Version:    0.7.0.1
 */