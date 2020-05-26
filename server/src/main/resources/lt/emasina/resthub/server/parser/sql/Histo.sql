with
p___ as (
select ${min} min0___, ${max} max0___, ${bins} steps___ from dual
),
q___ (x___) as ( select ${col} from (${sql})),
r___ as (

select
    min(min___) as min___,
    max(max___) as max___,
    (min(max___) - min(min___)) / min(steps___) as step___,
    min(mean___) as mean___,
    SQRT(sum(POWER(x___ - mean___, 2)) / min(count___)) as rms___
from
    q___,
    (select
        min(nvl(min0___, x___)) as min___,
        max(nvl(max0___, x___)) as max___,
        min(steps___) steps___,
        avg(x___) as mean___,
        count(*) as count___
    from
        q___, p___
    where
        (x___ >= min0___ and x___ < max0___) or
        min0___ is null or max0___ is null)
)

select
    bnum___ as bin,
    mean___ as range_mean,
    rms___ as range_rms,
    DECODE(bnum___, 0, null, steps___ + 1, null, min___ + (bnum___ - 1) * step___ + step___ / 2) as bin_mean,
    DECODE(bnum___, 0, null, min___ + (bnum___ - 1) * step___) as bin_from,
    DECODE(bnum___, steps___ + 1, null, min___ + (bnum___ - 0) * step___) as bin_to,
    nvl(bin_count___, 0) as bin_count
from
    r___,
    p___,
    (select rownum - 1 as bnum___  from p___ connect by rownum <= steps___ + 2)
    left join (
        select
            bitem___,
            count(bitem___) bin_count___
        from
            (select
                x___,
                WIDTH_BUCKET(x___, min___, max___, steps___) bitem___
            from
                p___, q___, r___)
                group by
                    bitem___)
     on bnum___ = bitem___
     order by
        bnum___ asc
;