package org.yamj.core.database.model;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "subtitle",
    uniqueConstraints= @UniqueConstraint(name="UIX_SUBTITLE_NATURALID", columnNames={"mediafile_id", "stagefile_id", "counter"})
)
public class Subtitle extends AbstractIdentifiable implements Serializable {

    private static final long serialVersionUID = -6279878819525772005L;
    
    @NaturalId
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mediafile_id", nullable = false)
    @ForeignKey(name = "FK_SUBTITLE_MEDIAFILE")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MediaFile mediaFile;
    
    @NaturalId
    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "FK_SUBITLE_STAGEFILE")
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "stagefile_id")
    private StageFile stageFile;

    @NaturalId
    @Column(name = "counter", nullable = false)
    private int counter = -1;

    @Column(name = "format", nullable = false)
    private String format;
    
    @Column(name = "language")
    private String language;

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    public StageFile getStageFile() {
        return stageFile;
    }

    public void setStageFile(StageFile stageFile) {
        this.stageFile = stageFile;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    
    // EQUALITY CHECKS
    
    @Override
    public int hashCode() {
        final int prime = 7;
        int result = 1;
        result = prime * result + (mediaFile == null ? 0 : mediaFile.hashCode());
        result = prime * result + (stageFile == null ? 0 : stageFile.hashCode());
        result = prime * result + counter;
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof Subtitle)) {
            return false;
        }
        Subtitle castOther = (Subtitle) other;
        // first check the id
        if ((this.getId() > 0) && (castOther.getId() > 0)) {
            return this.getId() == castOther.getId();
        }
        // check counter
        if (this.counter != castOther.counter) {
            return false;
        }
        // check media file
        if (this.mediaFile != null && castOther.mediaFile != null && !this.mediaFile.equals(castOther.mediaFile)) {
            return false;
        }
        // check stage file
        if (this.stageFile == null && castOther.stageFile != null) {
            return false;
        }
        if (this.stageFile != null && castOther.stageFile == null) {
            return false;
        }
        if (this.stageFile != null && castOther.stageFile != null) {
            return this.stageFile.equals(castOther.stageFile);
        }
        // both stage files are null
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Subtitle [ID=");
        sb.append(getId());
        if (getMediaFile() != null && Hibernate.isInitialized(getMediaFile())) {
            sb.append(", mediaFile='");
            sb.append(getMediaFile().getFileName());
            sb.append("'");
        }
        if (getStageFile() != null && Hibernate.isInitialized(getStageFile())) {
            sb.append(", stageFile='");
            sb.append(getStageFile().getFullPath());
            sb.append("'");
        }
        sb.append("', counter=");
        sb.append(getCounter());
        sb.append(", format=");
        sb.append(getFormat());
        sb.append(", language=");
        sb.append(getLanguage());
        sb.append("]");
        return sb.toString();
    }
}
