package serializers;

import data.media.Image;
import data.media.Media;
import data.media.MediaContent;
import org.fressian.FressianReader;
import org.fressian.FressianWriter;
import org.fressian.Reader;
import org.fressian.Writer;
import org.fressian.handlers.ILookup;
import org.fressian.handlers.ReadHandler;
import org.fressian.handlers.WriteHandler;
import org.fressian.impl.Fns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Fressian {
    public static void register(TestGroups groups) {
        groups.media.add(JavaBuiltIn.mediaTransformer, Fressian.<MediaContent>GenericSerializer());
    }

    public static <T> Serializer<T> GenericSerializer() {
        @SuppressWarnings("unchecked")
        Serializer<T> s = (Serializer<T>) GenericSerializer;
        return s;
    }

    public static class MediaContentReader implements ReadHandler {

        public Object read(Reader reader, Object o, int i) throws IOException {
            return new MediaContent((Media) reader.readObject(), (List<Image>) reader.readObject());
        }
    }

    public static class MediaContentWriter implements WriteHandler {

        public void write(Writer writer, Object o) throws IOException {
            MediaContent mc = (MediaContent) o;
            writer.writeTag("mc", 2);
            writer.writeObject(mc.getMedia());
            writer.writeObject(mc.getImages());
        }
    }

    public static class MediaReader implements ReadHandler {

        public Object read(Reader reader, Object o, int i) throws IOException {
            return new Media((String)reader.readObject(),
                    (String)reader.readObject(),
                    (int) reader.readInt(),
                    (int) reader.readInt(),
                    (String) reader.readObject(),
                    reader.readInt(),
                    reader.readInt(),
                    (int) reader.readInt(),
                    reader.readBoolean(),
                    (List<String>) reader.readObject(),
                    Media.Player.valueOf((String)reader.readObject()),
                    (String) reader.readObject()
            );
        }
    }

    public static class MediaWriter implements WriteHandler {

        public void write(Writer writer, Object o) throws IOException {
            Media m = (Media) o;
            writer.writeTag("m", 12);
            writer.writeObject(m.uri);
            writer.writeObject(m.title);
            writer.writeInt(m.width);
            writer.writeInt(m.height);
            writer.writeObject(m.format);
            writer.writeInt(m.duration);
            writer.writeInt(m.size);
            writer.writeInt(m.bitrate);
            writer.writeBoolean(m.hasBitrate);
            writer.writeObject(m.persons);
            writer.writeObject(m.player.toString());
            writer.writeObject(m.copyright);
        }
    }

    public static class ImageReader implements ReadHandler {

        public Object read(Reader reader, Object o, int i) throws IOException {
            return new Image((String)reader.readObject(),
                    (String)reader.readObject(),
                    (int) reader.readInt(),
                    (int) reader.readInt(),
                    Image.Size.valueOf((String)reader.readObject())
            );
        }
    }

    public static class ImageWriter implements WriteHandler {

        public void write(Writer writer, Object o) throws IOException {
            Image i = (Image) o;
            writer.writeTag("i", 5);
            writer.writeObject(i.uri);
            writer.writeObject(i.title);
            writer.writeInt(i.width);
            writer.writeInt(i.height);
            writer.writeObject(i.size.toString());
        }
    }

    public static ReadHandler mediaContentReader = new MediaContentReader();
    public static Map<String, WriteHandler> mediaContentWriter = Fns.soloMap("mc", (WriteHandler) new MediaContentWriter());
    public static ReadHandler mediaReader = new MediaReader();
    public static Map<String, WriteHandler> mediaWriter = Fns.soloMap("m", (WriteHandler) new MediaWriter());
    public static ReadHandler imageReader = new ImageReader();
    public static Map<String, WriteHandler> imageWriter = Fns.soloMap("i", (WriteHandler) new ImageWriter());


    public static ILookup<Object, ReadHandler> readHandlers = new ILookup() {
        public ReadHandler valAt(Object o) {
            if (o.equals("mc")) return mediaContentReader;
            if (o.equals("m")) return mediaReader;
            if (o.equals("i")) return imageReader;

            return null;
        }
    };

    public static ILookup<Class, Map<String, WriteHandler>> writeHandlers = new ILookup<Class, Map<String, WriteHandler>>() {
        public Map<String, WriteHandler> valAt(Class c) {
            if (c.equals(MediaContent.class)) return mediaContentWriter;
            if (c.equals(Media.class)) return mediaWriter;
            if (c.equals(Image.class)) return imageWriter;
            return null;
        }
    };

    // ------------------------------------------------------------
    // Serializer (just one)

    public static Serializer<Object> GenericSerializer = new Serializer<Object>() {
        public Object deserialize(byte[] array) throws Exception {
            ByteArrayInputStream in = new ByteArrayInputStream(array);
            Reader fin = new FressianReader(in, readHandlers);
            return fin.readObject();
        }

        public byte[] serialize(Object data) throws java.io.IOException {
            ByteArrayOutputStream out = outputStream(data);
            Writer fout = new FressianWriter(out, writeHandlers);
            fout.writeObject(data);
            return out.toByteArray();
        }

        public String getName() {
            return "fressian";
        }
    };
}
