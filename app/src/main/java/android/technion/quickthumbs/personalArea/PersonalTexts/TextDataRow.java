package android.technion.quickthumbs.personalArea.PersonalTexts;

public class TextDataRow {
    private String title;
    private String themeName;
    private String text;
    private double rating;
    private String numberOfTimesPlayed;
    private String bestScore;
    private String fastestSpeed;
    public boolean isClicked;
    public boolean isExpanded;

    public TextDataRow(String title,String themeName, String text, double rating,
                       String numberOfTimesPlayed, String bestScore, String fastestSpeed) {
        this.setTitle(title);
        this.setThemeName(themeName);
        this.setText(text);
        this.setRating(rating);
        this.setNumberOfTimesPlayed(numberOfTimesPlayed);
        this.setBestScore(bestScore);
        this.setFastestSpeed(fastestSpeed);
        this.isClicked = false;
        this.isExpanded = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setNumberOfTimesPlayed(String numberOfTimesPlayed) {
        this.numberOfTimesPlayed = numberOfTimesPlayed;
    }

    public String getNumberOfTimesPlayed() {
        return numberOfTimesPlayed;
    }

    public String getBestScore() {
        return bestScore;
    }

    public void setBestScore(String bestScore) {
        this.bestScore = bestScore;
    }

    public String getFastestSpeed() {
        return fastestSpeed;
    }

    public void setFastestSpeed(String fastestSpeed) {
        this.fastestSpeed = fastestSpeed;
    }
}
