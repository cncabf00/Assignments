#coding:utf-8
import os
import sys


class Rule(object):
    """docstring for Rule"""
    def __init__(self, left):
        super(Rule, self).__init__()
        self.left = left
        self.right = []


class Word(object):
    """docstring for Word"""
    def __init__(self, name):
        super(Word, self).__init__()
        self.name = name


class ActiveArc(object):
    """docstring for ActiveArc"""
    def __init__(self, rule, pos):
        super(ActiveArc, self).__init__()
        self.rule = rule
        self.pos = pos


class Item(object):
    """docstring for Item"""
    def __init__(self, name):
        super(Item, self).__init__()
        self.name = name


def chartParse(x):
    chart = []
    agenda = []
    activearcs = []
    words = x.split(' ')
    length = len(words)
    pos = 0
    while pos < length or len(agenda) != 0:
        if len(agenda) == 0:
            i = Item(dic[words[pos]].pos)
            i.start = pos
            i.end = pos + 1
            agenda.append(i)
            pos += 1
        x = agenda[0]
        agenda.remove(x)
        for rule in rules:
            if rule.right[0] == x.name:
                if len(rule.right) == 1:
                    i = Item(rule.left)
                    i.start = x.start
                    i.end = x.end
                    agenda.append(i)
                else:
                    arc = ActiveArc(rule, 0)
                    arc.start = x.start
                    arc.end = x.end
                    activearcs.append(arc)
        chart.append(x)
        for arc in activearcs:
            if arc.rule.right[arc.pos + 1] == x.name and arc.end == x.start:
                if (len(arc.rule.right) > arc.pos + 2):
                    newArc = ActiveArc(rule, arc.pos + 1)
                    newArc.start = arc.start
                    newArc.end = x.end
                    activearcs.append(newArc)
                else:
                    i = Item(arc.rule.left)
                    i.start = arc.start
                    i.end = x.end
                    agenda.append(i)
    for item in chart:
        print item.name, "from", item.start, "to", item.end


path = os.path.split(os.path.realpath(__file__))[0]


dic = {}
filename = path + os.path.sep + 'dict.txt'
if os.path.isfile(filename):
    dic_file = file(filename)
else:
    print 'failed loading dictionary: ' + filename + " doesn't exist"
    sys.exit(0)

while True:
    line = dic_file.readline()
    if len(line) == 0:
        break
    strs = line.split(' ')
    w = Word(strs[0])
    w.pos = strs[1].strip()
    dic[w.name] = w
dic_file.close()

rules = []
filename = path + os.path.sep + 'rule.txt'
if os.path.isfile(filename):
    rule_file = file(filename)
else:
    print 'failed loading rules: ' + filename + " doesn't exist"
    sys.exit(0)

while True:
    line = rule_file.readline()
    if len(line) == 0:
        break
    strs = line.split(',')
    r = Rule(strs[0])
    rights = strs[1].split(' ')
    for s in rights:
        r.right.append(s.strip())
    rules.append(r)
rule_file.close()

print "current only 'the cat caught a mouse' could work due to the incomplete dictionary and rules"
while True:
    print "please input a sentence(case sensitive):"
    x = raw_input()
    chartParse(x)
