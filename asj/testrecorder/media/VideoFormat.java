/*   1:    */ package asj.testrecorder.media;
/*   2:    */ 
/*   3:    */ public class VideoFormat
/*   4:    */   extends Format
/*   5:    */ {
/*   6:    */   private final int width;
/*   7:    */   private final int height;
/*   8:    */   private final int depth;
/*   9:    */   private final Class dataClass;
/*  10:    */   private final String encoding;
/*  11:    */   private final String compressorName;
/*  12:    */   public static final String IMAGE = "image";
/*  13:    */   public static final String QT_CINEPAK = "cvid";
/*  14:    */   public static final String QT_JPEG = "jpeg";
/*  15:    */   public static final String QT_JPEG_COMPRESSOR_NAME = "Photo - JPEG";
/*  16:    */   public static final String QT_PNG = "png ";
/*  17:    */   public static final String QT_PNG_COMPRESSOR_NAME = "PNG";
/*  18:    */   public static final String QT_ANIMATION = "rle ";
/*  19:    */   public static final String QT_ANIMATION_COMPRESSOR_NAME = "Animation";
/*  20:    */   public static final String QT_RAW = "raw ";
/*  21:    */   public static final String QT_RAW_COMPRESSOR_NAME = "NONE";
/*  22:    */   public static final String AVI_DIB = "DIB ";
/*  23:    */   public static final String AVI_RLE = "RLE ";
/*  24:    */   public static final String AVI_TECHSMITH_SCREEN_CAPTURE = "tscc";
/*  25:    */   public static final String AVI_MJPG = "MJPG";
/*  26:    */   public static final String AVI_PNG = "png ";
/*  27:    */   
/*  28:    */   public VideoFormat(String encoding, Class dataClass, int width, int height, int depth)
/*  29:    */   {
/*  30: 63 */     this(encoding, encoding, dataClass, width, height, depth);
/*  31:    */   }
/*  32:    */   
/*  33:    */   public VideoFormat(String encoding, String compressorName, Class dataClass, int width, int height, int depth)
/*  34:    */   {
/*  35: 67 */     this.encoding = encoding;
/*  36: 68 */     this.compressorName = compressorName;
/*  37: 69 */     this.dataClass = dataClass;
/*  38: 70 */     this.width = width;
/*  39: 71 */     this.height = height;
/*  40: 72 */     this.depth = depth;
/*  41:    */   }
/*  42:    */   
/*  43:    */   public VideoFormat(String encoding, String compressorName)
/*  44:    */   {
/*  45: 76 */     this(encoding, compressorName, null, -1, -1, -1);
/*  46:    */   }
/*  47:    */   
/*  48:    */   public VideoFormat(String encoding)
/*  49:    */   {
/*  50: 80 */     this(encoding, encoding, null, -1, -1, -1);
/*  51:    */   }
/*  52:    */   
/*  53:    */   public int getDepth()
/*  54:    */   {
/*  55: 84 */     return this.depth;
/*  56:    */   }
/*  57:    */   
/*  58:    */   public int getHeight()
/*  59:    */   {
/*  60: 88 */     return this.height;
/*  61:    */   }
/*  62:    */   
/*  63:    */   public int getWidth()
/*  64:    */   {
/*  65: 92 */     return this.width;
/*  66:    */   }
/*  67:    */   
/*  68:    */   public Class getDataClass()
/*  69:    */   {
/*  70: 97 */     return this.dataClass;
/*  71:    */   }
/*  72:    */   
/*  73:    */   public String getEncoding()
/*  74:    */   {
/*  75:101 */     return this.encoding;
/*  76:    */   }
/*  77:    */   
/*  78:    */   public String getCompressorName()
/*  79:    */   {
/*  80:104 */     return this.compressorName;
/*  81:    */   }
/*  82:    */ }

