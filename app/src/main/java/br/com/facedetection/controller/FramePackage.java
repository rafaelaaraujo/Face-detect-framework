package br.com.facedetection.controller;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class FramePackage {
	List<Bitmap> frameList;
	
	public FramePackage() {
		frameList = new ArrayList<Bitmap>();
	}
	
	// Manager images list
		// ADD ==================================================
	public boolean addImage(Bitmap frame) {
		frameList.add(frame);
		return true;
	}

	public boolean addImageAt(Bitmap frame, int index) {
		if (index > 0 && index <= frameList.size()) {
			frameList.add(index - 1, frame);
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
