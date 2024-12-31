package net.osmand.aidlapi.navigation;

import android.os.Bundle;
import android.os.Parcel;

import net.osmand.aidlapi.AidlParams;

import nodomain.freeyourgadget.gadgetbridge.model.NavigationInfoSpec;

public class ADirectionInfo extends AidlParams {

	private int distanceTo; //distance to next turn
	private NavigationInfoSpec.Action turnType; //turn type
	private boolean isLeftSide; //is movement left-sided

	public ADirectionInfo(int distanceTo, NavigationInfoSpec.Action turnType, boolean isLeftSide) {
		this.distanceTo = distanceTo;
		this.turnType = turnType;
		this.isLeftSide = isLeftSide;
	}

	protected ADirectionInfo(Parcel in) {
		readFromParcel(in);
	}

	public static final Creator<ADirectionInfo> CREATOR = new Creator<ADirectionInfo>() {
		@Override
		public ADirectionInfo createFromParcel(Parcel in) {
			return new ADirectionInfo(in);
		}

		@Override
		public ADirectionInfo[] newArray(int size) {
			return new ADirectionInfo[size];
		}
	};

	public int getDistanceTo() {
		return distanceTo;
	}

	public NavigationInfoSpec.Action getTurnType() {
		return turnType;
	}

	public boolean isLeftSide() {
		return isLeftSide;
	}

	public void setDistanceTo(int distanceTo) {
		this.distanceTo = distanceTo;
	}

	public void setTurnType(NavigationInfoSpec.Action turnType) {
		this.turnType = turnType;
	}

	public void setLeftSide(boolean leftSide) {
		isLeftSide = leftSide;
	}

	@Override
	protected void readFromBundle(Bundle bundle) {
		distanceTo = bundle.getInt("distanceTo");
		turnType = NavigationInfoSpec.Action.fromId(bundle.getInt("turnType"));
		isLeftSide = bundle.getBoolean("isLeftSide");
	}

	@Override
	public void writeToBundle(Bundle bundle) {
		bundle.putInt("distanceTo", distanceTo);
		bundle.putInt("turnType", turnType.id);
		bundle.putBoolean("isLeftSide", isLeftSide);
	}
}