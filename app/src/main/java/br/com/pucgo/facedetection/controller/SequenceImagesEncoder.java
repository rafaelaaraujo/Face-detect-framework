package br.com.pucgo.facedetection.controller;

import android.graphics.Bitmap;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.RgbToYuv420;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static org.jcodec.common.model.ColorSpace.RGB;

public class SequenceImagesEncoder {
	private SeekableByteChannel ch;
    private Picture toEncode;
    private RgbToYuv420 transform;
    private H264Encoder encoder;
    private ArrayList<ByteBuffer> spsList;
    private ArrayList<ByteBuffer> ppsList;
    private FramesMP4MuxerTrack outTrack;
    private ByteBuffer _out;
    private int frameNo = 1;
    private MP4Muxer muxer;
    
    private int screenWidth;
    private int screenHeight;
    
    private int timescale = 20;
    private int timeEachFrame = 0;
    private int duration = 1;
    private int totalDuration = 0;
    // SET functions ===========================================================   
    public void setTimeEachFrame(int timeEachFrame) {
    	this.timescale = 10;
    	this.timeEachFrame += timeEachFrame;
    	this.duration = timeEachFrame;
    	this.totalDuration += timeEachFrame;
    }
    
    public void setScreen(int screenWidth, int screenHeight) {
    	this.screenWidth = screenWidth;
    	this.screenHeight = screenHeight;
    }
    
    public void setScreenWidth(int screenWidth) {
    	this.screenWidth = screenWidth;
    }
    
    public void setScreenHeight(int screenHeight) {
    	this.screenHeight = screenHeight;
    }
    
    // GET functions ===========================================================  
    public int getScreenWidth() {
    	return this.screenWidth;
    }
    
    public int getScreenHeight() {
    	return this.screenHeight;
    }
    public int getDuration() {
    	return this.totalDuration;
    }
    
    // Constructor =============================================================
    public SequenceImagesEncoder(File out, int screenWidth, int screenHeight) throws IOException {
        this.ch = NIOUtils.writableFileChannel(out);

        // Transform to convert between RGB and YUV
        transform = new RgbToYuv420(0, 0);

        // Muxer that will store the encoded frames
        muxer = new MP4Muxer(ch, Brand.MP4);

        // Add video track to muxer
        outTrack = muxer.addTrackForCompressed(TrackType.VIDEO, timescale);

        // Allocate a buffer big enough to hold output frames
        _out = ByteBuffer.allocate(screenWidth * screenHeight * 6);

        // Create an instance of encoder
        encoder = new H264Encoder();

        // Encoder extra data ( SPS, PPS ) to be stored in a special place of
        // MP4
        spsList = new ArrayList<ByteBuffer>();
        ppsList = new ArrayList<ByteBuffer>();

    }

    public void encodeImage(Bitmap bi, int timeEachFrame) throws IOException {
    	setTimeEachFrame(timeEachFrame);
    	
        if (toEncode == null) {
            toEncode = Picture.create(bi.getWidth(), bi.getHeight(), ColorSpace.YUV420);
        }

        // Perform conversion
        for (int i = 0; i < 3; i++)
            Arrays.fill(toEncode.getData()[i], 0);
        transform.transform(fromBufferedImage(bi), toEncode);

        // Encode image into H.264 frame, the result is stored in '_out' buffer
        _out.clear();
        ByteBuffer result = encoder.encodeFrame(_out, toEncode);

        // Based on the frame above form correct MP4 packet
        spsList.clear();
        ppsList.clear();
        H264Utils.encodeMOVPacket(result, spsList, ppsList);

        outTrack.addFrame(new MP4Packet(result,
                frameNo, 	// frameNo * (this.timeEachFrame) = 5s, image will stop at second 5 and show the next image
                timescale, 							// set default = 1. How many frame per duration: timescale = 2 duration = 1 => 0.5s show 1 image
                duration, 							// auto-increase each time current duration = duration + pass duration.
                frameNo,
                true,
                null,
                frameNo,
                0));

    }

    public void finish() throws IOException {
        // Push saved SPS/PPS to a special storage in MP4
        outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList));

        
        
        // Write MP4 header and finalize recording
        muxer.writeHeader();
        NIOUtils.closeQuietly(ch);
    }
    
    // 
    public static Picture fromBufferedImage(Bitmap src) {
        Picture dst = Picture.create(src.getWidth(), src.getHeight(), RGB);
        fromBufferedImage(src, dst);
        return dst;
    }

    public static void fromBufferedImage(Bitmap src, Picture dst) {
        int[] dstData = dst.getPlaneData(0);

        int off = 0;
        for (int i = 0; i < src.getHeight(); i++) {
            for (int j = 0; j < src.getWidth(); j++) {
                int rgb1 = src.getPixel(j, i);
                dstData[off++] = (rgb1 >> 16) & 0xff;
                dstData[off++] = (rgb1 >> 8) & 0xff;
                dstData[off++] = rgb1 & 0xff;
            }
        }
    }
}
