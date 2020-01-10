package androidCourse.technion.quickthumbs.multiplayerSearch;

public class SearchingGrouper {
    public String textId;
    public int targetRoomSize;
    public int currentSize;

    public SearchingGrouper() {
    }

    public SearchingGrouper(String textId, int targetRoomSize, int currentSize) {
        this.textId = textId;
        this.targetRoomSize = targetRoomSize;
        this.currentSize = currentSize;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }

    public int getTargetRoomSize() {
        return targetRoomSize;
    }

    public void setTargetRoomSize(int targetRoomSize) {
        this.targetRoomSize = targetRoomSize;
    }
}

