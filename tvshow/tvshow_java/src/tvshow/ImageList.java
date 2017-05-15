/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tvshow;

/**
 *
 * @author Root
 */
public class ImageList {

    public int s_width;
    public int s_height;
    public byte[] bs;

    public int start;

    public int size;

    public ImageList(byte[] _bs) {
        bs = _bs;
        start = 0;

        byte version = (byte) bs[start++];
        byte intsize = bs[start++];

        s_width = bs[start++] & 0xFF;
        s_width |= (bs[start++] & 0xFF) << 8;

        start++;
        start++;

        s_height = bs[start++] & 0xFF;
        s_height |= (bs[start++] & 0xFF) << 8;

        start++;
        start++;

        start++;
        start++;
        start++;
        start++;

        size = (bs.length - start) / (s_width * s_height * 3 + 4);
    }

    public int size() {
        return size;
    }

    public Image getImage(int index) {
        return new Image(bs, start + index * (s_width * s_height * 3 + 4), s_width, s_height);
    }

    public final static class Image {

        final byte[] bs;
        final int offset;
        final int width;
        final int height;

        public Image(byte[] _bs, int _offset, int _width, int _height) {
            bs = _bs;
            offset = _offset;
            width = _width;
            height = _height;
        }

        public Image(int time, int _width, int _height) {
            offset = 0;
            width = _width;
            height = _height;
            bs = new byte[_width * _height * 3 + 4];

            bs[offset + 0] = (byte) (time & 0xFF);
            bs[offset + 1] = (byte) ((time >> 8) & 0xFF);
            bs[offset + 2] = (byte) ((time >> 16) & 0xFF);
            bs[offset + 3] = (byte) ((time >> 24) & 0xFF);

        }

        public final int getTime() {
            int ms = bs[offset + 0] & 0xFF;
            ms |= (bs[offset + 1] & 0xFF) << 8;
            ms |= (bs[offset + 2] & 0xFF) << 16;
            ms |= (bs[offset + 3] & 0xFF) << 24;
            return ms;
        }

        public final int getWidth() {
            return width;
        }

        public final int getHeight() {
            return height;
        }

        public final int getRGB(int w, int h) {
            int os = offset + 4 + (w + h * width) * 3;
            int rgb = (bs[os + 0] & 0xFF) << 16;
            rgb |= (bs[os + 1] & 0xFF) << 8;
            rgb |= (bs[os + 2] & 0xFF);
            rgb |= (0xFF) << 24;
            return rgb;
        }

        public final void setRGB(int w, int h, int rgb) {
            int os = offset + 4 + (w + h * width) * 3;

            bs[os + 0] = (byte) ((rgb >> 16) & 0xFF);
            bs[os + 1] = (byte) ((rgb >> 8) & 0xFF);
            bs[os + 2] = (byte) (rgb & 0xFF);
        }

        @Override
        public final Image clone() {
            Image img = new Image(this.getTime(), this.getWidth(), this.getHeight());

            for (int h = 0; h < getHeight(); h++) {
                for (int w = 0; w < getWidth(); w++) {
                    img.setRGB(w, h, getRGB(w, h));
                }
            }
            return img;
        }
    }

}
