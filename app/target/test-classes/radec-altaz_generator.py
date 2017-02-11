from astropy import units as u
from astropy.coordinates import AltAz
from astropy.coordinates import EarthLocation, SkyCoord
from astropy.time import Time
from random import randint

observing_location = EarthLocation(lat='52.2', lon='351.6', height=100 * u.m)
observing_time = Time('2017-02-05 20:12:18')
aa = AltAz(location=observing_location, obstime=observing_time)

with open("radec-altaz.csv", 'w') as f:
    for i in range(100):
        ra_hour = randint(0, 23)
        ra_min = randint(0, 59)
        dec_deg = randint(-70, 70)
        dec_min = randint(0, 59)
        coord = SkyCoord('{}h{}m'.format(ra_hour, ra_min), '{}d{}m'.format(dec_deg, dec_min))
        altaz = coord.transform_to(aa)
        f.write('{},{},{},{}\n'.format(coord.ra.hour, coord.dec.deg, altaz.alt.deg, altaz.az.deg))
