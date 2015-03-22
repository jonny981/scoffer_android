package com.scoff.scoffer;

import com.google.common.collect.ComparisonChain;
import java.util.Comparator;

public class OfferComparator implements Comparator<OfferItem> {
    public int compare(OfferItem offer1, OfferItem offer2) {
        return ComparisonChain.start()
                .compare(offer1.getDistance(), offer2.getDistance())
                .compare(offer1.getCompanyName(), offer2.getCompanyName())
                .compare(offer1.getTitle(), offer2.getTitle())
                .result();
    }
}
