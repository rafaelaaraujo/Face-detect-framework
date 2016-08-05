package br.com.pucgo.facedetection.controller;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class FramePackage {
	List<Bitmap> frameList;
	
	public FramePackage() {
		// TODO Auto-generated constructor stub
		frameList = new ArrayList<Bitmap>();
	}
	
	// Manager images list
		// ADD ==================================================
	public boolean addImage(Bitmap frame) {
		frameList.add(frame);
		return true;
	}
	
//	public boolean addImage(String imgName, int duration) {
//		Frame frame = new Frame(imgName, duration);
//		frameList.add(frame);
//		return true;
//	}
//
	public boolean addImageAt(Bitmap frame, int index) {
		if (index > 0 && index <= frameList.size()) {
			frameList.add(index - 1, frame);
			return true;
		}
		return false;
	}
	
//	public boolean addImageAt(String imgName, int duration, int index) {
//		if (index > 0 && index <= frameList.size()) {
//			Frame frame = new Frame(imgName, duration);
//			frameList.add(index - 1, frame);
//			return true;
//		}
//		return false;
//	}
		// REMOVE ===============================================
	public boolean removeImage(int index) {
		if (index > 0 && index <= frameList.size()) {
			frameList.remove(index - 1);
			return true;
		}
		return false;
	}
	
		// GET ==================================================
	public Bitmap getFrameAt(int index) {
		if (index > 0 && index <= frameList.size()) {
			return frameList.get(index - 1);
		}
		return null;
	}
	
	public int getCount() {
		return frameList.size();
	}
}
