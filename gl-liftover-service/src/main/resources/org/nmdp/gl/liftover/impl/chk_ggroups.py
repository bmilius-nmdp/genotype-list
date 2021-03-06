#!/usr/bin/env python3

import collections


def main():
    ggroupids = set()
    ggroupids_list = []
    with open('g-group-ids.txt', 'r') as f:
        linecount = 0
        for line in f:
            linecount += 1
            ggroup = line.split()[0]
            # starting in 3.28, g-group-ids.txt started prepending with 'HLA-A'
            # so adding this for the comparison
            if ggroup[:4] == 'HLA-':
                ggroup = ggroup[4:]
            ggroupids.add(ggroup)
            ggroupids_list.append(ggroup)
        print("# of lines in g-group-ids.txt =", linecount)

    ggroups = set()
    ggroups_list = []
    with open('g-groups.txt', 'r') as f:
        linecount = 0
        for line in f:
            linecount += 1
            ggroup = line.split()[0]
            ggroups.add(ggroup)
            ggroups_list.append(ggroup)
        print("# of lines in g-groups.txt =", linecount)

    print('number of unique G-groups in g-group-ids.txt =', len(ggroupids))
    print('number of unique G-groups in g-groups.txt =', len(ggroups))

    print('duplicate G-groups in g-group-ids.txt')
    print(list.sort([item for item, count in collections.Counter(ggroupids_list).items()
           if count > 1]))

    print('duplicate G-groups in g-groups.txt')
    print(list.sort([item for item, count in collections.Counter(ggroups_list).items()
           if count > 1]))

    print('G-groups in g-group-ids.txt but not in g-groups.txt')
    for i in (sorted(ggroupids - ggroups)):
        print(i)

    print('G-groups in g-groups.txt but not in g-group-ids.txt')
    for i in (sorted(ggroups - ggroupids)):
        print(i)


if __name__ == '__main__':
    main()
