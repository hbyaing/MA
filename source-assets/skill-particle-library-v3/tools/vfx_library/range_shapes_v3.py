"""World-space footprints. Damage dimensions come from the recorded server predicates.

Contours are shared by the browser, recipe JSON and native ground-decal authoring.
They are never normalized to a common decorative circle.
"""
from __future__ import annotations
import math


def arc(cx, cz, radius, start=0.0, end=math.tau, segments=96):
    count = max(4, math.ceil(abs(end-start)/math.tau*segments))
    return [[round(cx+math.cos(start+(end-start)*i/count)*radius, 6),
             round(cz+math.sin(start+(end-start)*i/count)*radius, 6)] for i in range(count+1)]


def capsule(a, b, half_width):
    angle = math.atan2(b[2]-a[2], b[0]-a[0])
    return [arc(b[0], b[2], half_width, angle-math.pi/2, angle+math.pi/2, 48)
            + arc(a[0], a[2], half_width, angle+math.pi/2, angle+3*math.pi/2, 48)]


def footprint(event, actual=True):
    a = event.get('origin', [0,0,0]); b = event.get('target', a)
    shape = event.get('shape')
    values = event.get('collision', {}) if actual else event
    if shape == 'line':
        return capsule(a, b, values.get('width', event.get('width', .5))/2)
    if shape in ('circle','column','zone','ring','cone'):
        radius = values.get('radius', event.get('radius', 0))
        if radius is None or radius <= 0:
            return []
        if shape == 'cone':
            angle = math.atan2(b[2]-a[2], b[0]-a[0])
            half = math.radians(values.get('angleDegrees', event.get('angleDegrees', event.get('width', 0))))/2
            # Production cone() accepts the entire <0.01 block disc at its root,
            # even behind the cone. Build a single union contour, not an XOR hole.
            root_radius = min(radius, .01) if actual else 0
            if actual and math.hypot(b[0]-a[0], b[2]-a[2]) <= .0001:
                return [arc(a[0], a[2], root_radius)]
            if half >= math.pi:
                return [arc(a[0], a[2], radius)]
            curve = arc(a[0],a[2],radius,angle-half,angle+half)
            if root_radius:
                curve += arc(a[0],a[2],root_radius,angle+half,angle-half+math.tau)
                return [curve]
            return [[[a[0],a[2]]]+curve]
        outer = arc(a[0],a[2],radius)
        if shape == 'ring':
            # Collision radius/innerRadius in the extraction already include +/-0.25.
            inner = values.get('innerRadius', event.get('innerRadius', max(0,event.get('radius',radius)-event.get('width',0))))
            # Circumscribe the hole: an inscribed inner polygon would place a
            # thin damaging-looking crescent inside the genuinely safe hole.
            hole_radius = inner/math.cos(math.pi/96)
            return [outer, list(reversed(arc(a[0],a[2],hole_radius)))] if inner > 0 else [outer]
        return [outer]
    return []


def bounds(contours):
    points = [p for line in contours for p in line]
    if not points:
        return None
    return [min(p[0] for p in points),min(p[1] for p in points),
            max(p[0] for p in points),max(p[1] for p in points)]


def sample_outline(contours, spacing=1.7, budget=72):
    """Even-distance motif positions, never spanning a hole or a safe gap."""
    samples=[]
    for line in contours:
        carry=0
        for a,b in zip(line,line[1:]+line[:1]):
            length=math.dist(a,b)
            while carry < length:
                t=carry/max(length,1e-9)
                samples.append([a[0]+(b[0]-a[0])*t,a[1]+(b[1]-a[1])*t,
                                math.atan2(b[1]-a[1],b[0]-a[0])])
                carry+=spacing
            carry-=length
    if len(samples)>budget:
        samples=[samples[int(i*len(samples)/budget)] for i in range(budget)]
    return [[round(v,5) for v in p] for p in samples]


def active_at(event, tick):
    """Actual event lifetime, including server cancellation before hit tests."""
    start=event['startTick']
    # Recorded projectile endTick is authoritative: its duration counts both ends.
    end=event.get('endTick',start+event.get('durationTicks',0))
    if tick < start or tick >= event.get('cancelAtTick',math.inf):
        return False
    return tick <= end if event.get('endInclusive',False) else tick < end


def contains(event,x,z,tick=None,player_y=None,player_height=1.8):
    """Analytic source predicate; optional time and player vertical extent.

    Without a tick this is geometry-only. Gaze cannot be reduced to an XZ mask.
    """
    if tick is not None and not active_at(event,tick):
        return False
    a=event.get('origin',[0,0,0]); b=event.get('target',a); c=event.get('collision',event)
    if player_y is not None and (player_y > c.get('maxY',math.inf) or player_y+player_height < c.get('minY',-math.inf)):
        return False
    dx,dz=x-a[0],z-a[2]; dist=math.hypot(dx,dz); shape=event['shape']
    if shape=='line':
        vx,vz=b[0]-a[0],b[2]-a[2]; length=vx*vx+vz*vz
        t=max(0,min(1,(dx*vx+dz*vz)/length)) if length>1e-9 else 0
        return math.hypot(dx-t*vx,dz-t*vz)<=c.get('width',event.get('width',0))/2+1e-8
    if shape=='ring':
        inner=c.get('innerRadius',max(0,c['radius']-c.get('width',event.get('width',0))))
        return inner-1e-8 <= dist <= c['radius']+1e-8
    if shape=='cone':
        vx,vz=b[0]-a[0],b[2]-a[2]; length=math.hypot(vx,vz)
        return dist<=c['radius']+1e-8 and (dist<.01 or (length>.0001 and (dx*vx+dz*vz)/(dist*length)>=math.cos(math.radians(c['angleDegrees']/2))-1e-8))
    if shape in ('circle','column','zone'):
        return dist<=c['radius']+1e-8
    return False


def prepare_scenario(scenario):
    """Mutate a copied source scenario with authoring-only render data."""
    all_points=[[0,0], [scenario['inputs']['target'][0],scenario['inputs']['target'][2]]]
    for index,event in enumerate(scenario['events']):
        event['eventId']=f'e{index:03d}'
        if event.get('kind')=='hazard':
            event['footprint']=footprint(event)
            event['nominalFootprint']=footprint(event,False)
            event['bounds']=bounds(event['footprint'])
            event['edgeSamples']=sample_outline(event['footprint'])
            all_points.extend(p for line in event['footprint'] for p in line)
        if event.get('kind')=='projectile':
            all_points.extend([p[1][0],p[1][2]] for p in event.get('path',[]))
        if 'target' in event and event.get('kind')=='move':
            all_points.append([event['target'][0],event['target'][2]])
    scenario['viewBounds']=[min(p[0] for p in all_points)-3,min(p[1] for p in all_points)-3,
                            max(p[0] for p in all_points)+3,max(p[1] for p in all_points)+3]
    return scenario
