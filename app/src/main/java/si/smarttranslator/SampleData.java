package si.smarttranslator;

public class SampleData {
    public int numberRead;
    public int maxLength;
    public int newRecordingOffset;
    public int secondCopyLength;
    public int firstCopyLength;

    public SampleData(int numberRead, int maxLength, int newRecordingOffset, int secondCopyLength, int firstCopyLength) {
        this.numberRead = numberRead;
        this.maxLength = maxLength;
        this.newRecordingOffset = newRecordingOffset;
        this.secondCopyLength = secondCopyLength;
        this.firstCopyLength = firstCopyLength;
    }
}
