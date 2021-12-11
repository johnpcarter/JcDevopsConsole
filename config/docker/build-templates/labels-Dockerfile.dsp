#
# Template to add labels to image
#

FROM %value fromImage%

LABEL%loop labels% %value key%="%value value%" \%nl%%endloop%