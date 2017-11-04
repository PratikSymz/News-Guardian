/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pratiksymz.android.newsguardian;

/**
 * {@link NewsClass} represents a news article which will be shown in the news_list layout.
 * It contains a title, the news section, the web URL as well as the date and time it was
 * published.
 */

public class NewsClass {

    /**
     * The article's title
     */
    private String mTitle;

    /**
     * The section the article belongs to
     */
    private String mSectionName;

    /**
     * Url for the article's webpage
     */
    private String mWebURL;

    /**
     * Publication date and time of the article
     */
    private String mPublicationDate;

    /**
     * Create a new NewsItem object
     *
     * @param title           is the article's title
     * @param sectionName     is the section the article belongs to
     * @param webURL          is the article's webpage Url
     * @param publicationDate is the article's publication date
     */
    public NewsClass(String title, String sectionName, String webURL, String publicationDate) {
        mTitle = title;
        mSectionName = sectionName;
        mWebURL = webURL;
        mPublicationDate = publicationDate;
    }

    /**
     * Returns the Title of the news item.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the Section of the news item.
     */
    public String getSectionName() {
        return mSectionName;
    }

    /**
     * Returns the WebURL of the news item.
     */
    public String getWebURL() {
        return mWebURL;
    }

    /**
     * Returns the Publication Date of the news item.
     */
    public String getPublicationDate() {
        return mPublicationDate;
    }
}
