#
# Run update manager to add latest fixes for webMethods products installed /opt/sag dir
#

FROM %value SRC%

LABEL BUILD=%value id%

LABEL MAINTAINER="%value author% (%value email%)" \
	COMMENT="update manager executed to apply ALL latest fixes" \
	BUILD=%value build%

USER sagadmin

RUN /software/update.sh
