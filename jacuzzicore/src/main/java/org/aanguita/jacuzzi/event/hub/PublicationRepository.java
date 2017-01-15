package org.aanguita.jacuzzi.event.hub;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class PublicationRepository {

    private static class StoredPublication implements Comparable<StoredPublication> {

        private final Publication publication;

        private final Long liveUntil;

        public StoredPublication(Publication publication, Long keepMillis) {
            this.publication = publication;
            this.liveUntil = keepMillis == null ? null : publication.getTimestamp() + keepMillis;
        }

        public Publication getPublication() {
            return publication;
        }

        public Long getLiveUntil() {
            return liveUntil;
        }

        @Override
        public int compareTo(@NotNull PublicationRepository.StoredPublication o) {
            if (liveUntil == null) {
                return 1;
            } else if (o.liveUntil == null) {
                return -1;
            } else {
                return liveUntil.compareTo(o.liveUntil);
            }
        }
    }

    private final Queue<StoredPublication> storedStoredPublications;

    PublicationRepository() {
        storedStoredPublications = new PriorityQueue<>();
    }

    synchronized void storePublication(Publication publication, Long keepMillis) {
        purgePublications();
        if (mustStorePublication(keepMillis)) {
            storedStoredPublications.add(new StoredPublication(publication, keepMillis));
        }
    }

    private boolean mustStorePublication(Long keepMillis) {
        return keepMillis == null || keepMillis > 0L;
    }

    synchronized List<Publication> getStoredPublications(String... channelExpressions) {
        purgePublications();
        Set<Channel> channels = Arrays.stream(channelExpressions)
                .map(Channel::new)
                .collect(Collectors.toSet());
        List<Publication> matchingPublications = new ArrayList<>();
        for (StoredPublication storedPublication : storedStoredPublications) {
            for (Channel channel : channels) {
                if (channel.matches(storedPublication.getPublication().getChannel())) {
                    matchingPublications.add(storedPublication.getPublication());
                    break;
                }
            }
        }
        matchingPublications.sort((o1, o2) -> (int) (o1.getTimestamp() - o2.getTimestamp()));
        return matchingPublications;
    }

    private void purgePublications() {
        long now = System.currentTimeMillis();
        while (!storedStoredPublications.isEmpty() && storedStoredPublications.peek().getLiveUntil() != null && storedStoredPublications.peek().getLiveUntil() < now) {
            storedStoredPublications.remove();
        }
    }
}
