package bactimas.db.beans;

import java.math.BigDecimal;
import java.sql.Date;

public class Experiment {
	public BigDecimal getPictureScale() {
		return pictureScale;
	}
	public void setPictureScale(BigDecimal pictureScale) {
		this.pictureScale = pictureScale;
	}
	public BigDecimal getPixelWidthMicron() {
		return pixelWidthMicron;
	}
	public void setPixelWidthMicron(BigDecimal pixelWidthMicron) {
		this.pixelWidthMicron = pixelWidthMicron;
	}
	public BigDecimal getPixelHeightMicron() {
		return pixelHeightMicron;
	}
	public void setPixelHeightMicron(BigDecimal pixelHeightMicron) {
		this.pixelHeightMicron = pixelHeightMicron;
	}
	private int idExperiment;
	private String blueMovieFileName;
	private String redMovieFileName;
	private String greenMovieFileName;
	private int movieSpf;
	private String experimentName;
	private Date dateCreated;
	private BigDecimal pixelWidthMicron;
	private BigDecimal pixelHeightMicron;
	private BigDecimal pictureScale;
	
	public String getBlueMovieFileName() {
		return blueMovieFileName;
	}
	public void setBlueMovieFileName(String blueMovieFileName) {
		this.blueMovieFileName = blueMovieFileName;
	}
	public String getRedMovieFileName() {
		return redMovieFileName;
	}
	public void setRedMovieFileName(String redMovieFileName) {
		this.redMovieFileName = redMovieFileName;
	}
	public String getGreenMovieFileName() {
		return greenMovieFileName;
	}
	public void setGreenMovieFileName(String greenMovieFileName) {
		this.greenMovieFileName = greenMovieFileName;
	}

	
	public int getIdExperiment() {
		return idExperiment;
	}
	public void setIdExperiment(int idExperiment) {
		this.idExperiment = idExperiment;
	}


	public String getExperimentName() {
		return experimentName;
	}
	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(long dateCreated) {
		this.dateCreated = new Date(dateCreated);
	}
	
	public String toString() {
		return getExperimentName() + "(" + getDateCreated() + ") (id=" + idExperiment + ") ";
	}
	public int getMovieSpf() {
		return movieSpf;
	}
	public void setMovieSpf(int movieSpf) {
		this.movieSpf = movieSpf;
	}
	
}
