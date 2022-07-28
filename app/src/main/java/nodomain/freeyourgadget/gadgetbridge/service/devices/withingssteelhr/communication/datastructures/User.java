package nodomain.freeyourgadget.gadgetbridge.service.devices.withingssteelhr.communication.datastructures;

import java.nio.ByteBuffer;
import java.util.Date;

public class User extends WithingsStructure {

    // This is just a dummy value as this seems to be the withings account id,
    // which we do not need, but the watch expects:
    private int userID = 123456;
    private int weight;
    private int height;
    //Seems to be 0x00 for male and 0x01 for female. Found no other in my tests.
    private byte gender;
    private Date birthdate;
    private String name;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public short getLength() {
        return (short) ((name != null ? name.getBytes().length : 0) + 22);
    }

    @Override
    protected void fillinTypeSpecificData(ByteBuffer rawDataBuffer) {
        rawDataBuffer.putInt(userID);
        rawDataBuffer.putInt(weight);
        rawDataBuffer.putInt(height);
        rawDataBuffer.put(gender);
        rawDataBuffer.putInt((int)(birthdate.getTime()/1000));
        addStringAsBytesWithLengthByte(rawDataBuffer, name);
    }

    @Override
    public short getType() {
        return WithingsStructureType.USER;
    }
}
