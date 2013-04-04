package com.moviejukebox.core.database.dao;

import com.moviejukebox.core.database.model.MediaFile;
import com.moviejukebox.core.database.model.VideoData;
import com.moviejukebox.core.hibernate.ExtendedHibernateDaoSupport;
import java.sql.SQLException;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Service;

@Service("mediaDao")
public class MediaDao extends ExtendedHibernateDaoSupport {

    public VideoData getVideoData(final String identifier) {
        return this.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<VideoData>() {
            @Override
            public VideoData doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(VideoData.class);
                criteria.add(Restrictions.naturalId().set("identifier", identifier));
                criteria.setCacheable(true);
                criteria.setCacheMode(CacheMode.NORMAL);
                return (VideoData)criteria.uniqueResult();
            }
        });
    }

    public MediaFile getMediaFile(final String baseFileName) {
        return this.getHibernateTemplate().executeWithNativeSession(new HibernateCallback<MediaFile>() {
            @Override
            public MediaFile doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(MediaFile.class);
                criteria.add(Restrictions.naturalId().set("baseFileName", baseFileName));
                criteria.setCacheable(true);
                criteria.setCacheMode(CacheMode.NORMAL);
                return (MediaFile)criteria.uniqueResult();
            }
        });
    }
}
