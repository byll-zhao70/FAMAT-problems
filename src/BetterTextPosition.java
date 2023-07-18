public class BetterTextPosition {
    private String content;
    private double xDirAdj, yDirAdj, endY, height, width;

    public BetterTextPosition(String content, double xDirAdj, double yDirAdj, double endY, double height, double width) {
        this.content = content;
        this.xDirAdj = xDirAdj;
        this.yDirAdj = yDirAdj;
        this.endY = endY;
        this.height = height;
        this.width = width;
    }

    public String getContent() {
        return content;
    }

    public double getXDirAdj() {
        return xDirAdj;
    }

    public double getYDirAdj() {
        return yDirAdj;
    }
    public double getEndY() {
        return endY;
    }
    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setXDirAdj(int xDirAdj) {
        this.xDirAdj = xDirAdj;
    }

    public void setYDirAdj(int yDirAdj) {
        this.yDirAdj = yDirAdj;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
