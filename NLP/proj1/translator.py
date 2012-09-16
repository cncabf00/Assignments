#coding:utf-8
import re
import sys
import os
import platform

isWindows = platform.platform().lower().startswith('windows')

if isWindows:
    from ctypes import *

    WHITE = 0xf
    YELLO = 0xe
    MAGENTA = 0xd
    RED = 0xc
    CYAN = 0xb
    GREEN = 0xa
    BLUE = 0x9
    CYAN_DARK = 0x3
    GREEN_DARK = 0x2
    GREY = 0x8
    

    color_prompt = CYAN
    color_sys = CYAN_DARK
    color_word = GREEN
    color_pos = GREY
    color_error = RED
    color_info = MAGENTA
    color_trans = YELLO
    color_num = GREEN_DARK

    windll.Kernel32.GetStdHandle.restype = c_ulong
    h = windll.Kernel32.GetStdHandle(c_ulong(0xfffffff5))
    windll.Kernel32.SetConsoleTextAttribute(h, WHITE)
    # for color in xrange(1, 16):
    #     windll.Kernel32.SetConsoleTextAttribute(h, color)
    #     print 'hello'
else:
    WHITE = '\033[99m'
    YELLO = '\033[93m'
    MAGENTA = '\033[95m'
    RED = '\033[91m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    BLUE = '\033[91m'
    CYAN_DARK = '\033[36m'
    GREEN_DARK = '\033[32m'
    BLUE_DARK = '\033[34m'
    MAGENTA_DARK = '\033[35m'
    RED_DARK = '\033[31m'
    GREY = '\033[90m'
    ENDC = '\033[0m'

    color_prompt = MAGENTA_DARK
    color_sys = CYAN_DARK
    color_word = RED_DARK
    color_pos = GREY
    color_error = RED
    color_info = CYAN_DARK
    color_trans = BLUE_DARK
    color_num = GREEN_DARK

encoding = sys.getfilesystemencoding()

path = os.path.split(os.path.realpath(__file__))[0]


def printWithColor(str, color, blank=False):
    if isWindows:
        windll.Kernel32.SetConsoleTextAttribute(h, color)
        if blank:
            print str,
        else:
            print str
        windll.Kernel32.SetConsoleTextAttribute(h, WHITE)
    else:
        if blank:
            print color + str + ENDC,
        else:
            print color + str + ENDC


class Rule:
    pass


class Word(object):
    """docstring for Word"""
    def __init__(self, name):
        super(Word, self).__init__()
        self.name = name
        self.error = None
        self.action = None


class Translator(object):
    rules = None
    irregularRules = None
    """docstring for Translator"""
    def __init__(self):
        super(Translator, self).__init__()
        Translator.readRule()
        Translator.readIrregularRules()

    @staticmethod
    def readRule():
        # print '----------'
        # printWithColor('loading regular rules...', color_prompt)
        Translator.rules = []
        filename = path + os.path.sep + 'rule.txt'
        if os.path.isfile(filename):
            rule_file = file(filename)
        else:
            printWithColor('failed loading regular rules: '\
                 + filename + " doesn't exist", color_sys)
            return
        while True:
            line = rule_file.readline().rstrip()
            if len(line) == 0:
                break
            strs = line.split(' ')
            r = Rule()
            r.form = re.compile('(' + strs[0] + ')' + '(' + strs[1] + ')$')
            if len(strs) < 4:
                t = ""
            else:
                t = strs[2]
            r.replace = t
            r.description = strs[-1]
            Translator.rules.append(r)
        rule_file.close()
        print len(Translator.rules),
        printWithColor('\b' + ' regular rules are loaded', color_sys)

    @staticmethod
    def readIrregularRules():
        # print '----------'
        # print 'loading irregular rules...'
        Translator.irregularRules = {}
        filename = path + os.path.sep + 'irregular_rule.txt'
        if os.path.isfile(filename):
            rule_file = file(filename)
        else:
            printWithColor('failed loading irregular rules: '\
                 + filename + " doesn't exist", color_sys)
            return
        while True:
            line = rule_file.readline().rstrip()
            if len(line) == 0:
                break
            strs = line.split(' ', 2)
            r = Rule()
            r.form = strs[0]
            r.replace = strs[1]
            r.description = strs[2]
            if r.form not in Translator.irregularRules:
                Translator.irregularRules[r.form] = [r]
            else:
                Translator.irregularRules[r.form].append(r)
        rule_file.close()
        print len(Translator.irregularRules),
        printWithColor('\b' + ' irregular rules are loaded', color_sys)

    def restore(self, word):
        if Translator.irregularRules == None:
            Translator.readIrregularRules()
        if Translator.rules == None:
            Translator.readRule()
        array = []
        if word.name in Translator.irregularRules:
            for r in Translator.irregularRules[word.name]:
                restored = Word(word.name)
                newName = r.replace
                restored.action = 'restored by irregular rule from "' + \
                                        word.name + '" to "' + newName\
                                        + '"' + r.description
                if newName in dic:
                        restored.pos = dic[newName].pos
                        restored.trans = dic[newName].trans
                        restored.name = newName
                else:
                        restored.error = '"' + newName + '" is not in the dictionary'
                array.append(restored)
            return array
        # count = 0
        matchedOnce = False
        for r in Translator.rules:
            m = r.form.match(word.name)
            if m:
                # print 'rule %d matched' % count
                restored = Word(word.name)
                newName = m.groups(0)[0] + r.replace
                restored.action = 'restored by regular rule from "' + \
                                    word.name + '" to "' + newName\
                                    + '"' + r.description
                if newName in dic:
                    matchedOnce = True
                    restored.pos = dic[newName].pos
                    restored.trans = dic[newName].trans
                    restored.name = newName
                    array.append(restored)
            # count = count + 1
        if not matchedOnce:
            error = Word(word.name)
            error.error = 'word "' + word.name + '" does not exist in the '\
                            + 'dictionary or is not mentioned in the rules'
            array.append(error)
        return array

    def printWords(self, words, withLog=True):
        if len(words) > 1:
            count = 0
        else:
            count = None
        for w in words:
            if count != None:
                printWithColor("[%d]" % count, color_num)
                count = count + 1
            if withLog:
                if w.action != None:
                    printWithColor(w.action, color_sys)
                if w.error != None:
                    printWithColor(w.error + '\n', color_error)
                    return
                printWithColor(w.name, color_word)
                printWithColor(w.pos, color_pos)
                printWithColor(w.trans.decode('gbk').encode(encoding), color_trans)
            else:
                s = w.trans.split(',')[0].split(' ')[-1].decode('gbk').encode(encoding)
                print '\b' + s,
            print ""

    def translateWord(self, s, withLog=True):
        if s in dic:
            words = [dic[s]]
        else:
            w = Word(s)
            words = translator.restore(w)
        self.printWords(words, withLog)


print 'Welcome To Little Translator'
printWithColor('by MF1233055 - Zhou Haoyi', color_info)
print '------------------------------------------'

# print "loading dictionary..."
dic = {}
filename = path + os.path.sep + 'dic_ec.txt'
if os.path.isfile(filename):
    dic_file = file(filename)
else:
    printWithColor('failed loading dictionary: ' + filename + " doesn't exist", color_sys)
    sys.exit(0)
# reg_word_en = re.compile('^[a-zA-Z!\"\'().0-9-]*')
# reg_part_of_speech = re.compile('[a-z].')
while True:
    line = dic_file.readline()
    if len(line) == 0:
        break
    strs = line.split('\xff')
    w = Word(strs[0])
    w.pos = strs[1]
    w.trans = strs[2]
    dic[w.name] = w
print len(dic),
printWithColor("\b words are loaded", color_sys)
dic_file.close()

translator = Translator()

mode = -1
while True:
    if mode == -1:
        printWithColor('\nplease select a translation mode', color_sys)
        printWithColor('type in "w" for word, and "s" for sentence', color_prompt)
        x = raw_input()
        if x == 'w':
            mode = 0
        elif x == 's':
            mode = 1
    elif mode == 0:
        printWithColor('[W]', color_info, True)
        printWithColor('\bplease type in a word(case sensitive)', color_prompt, True)
        printWithColor('\b[\s to sentence mode, \q to quit)]:', color_sys)
        x = raw_input()
        if x == '\q':
            printWithColor('quit', color_sys)
            break
        elif x == '\s':
            printWithColor('switch to sentence mode\n', color_sys)
            mode = 1
            continue
        print '--------------------'
        printWithColor('Input:', color_sys, True)
        print '\b"' + x + '"\n',
        translator.translateWord(x)
    elif mode == 1:
        printWithColor('[S]', color_info, True)
        printWithColor('\bplease type in a sentence(case sensitive)', color_prompt, True)
        printWithColor('\b[\w to word mode, \q to quit]:', color_sys)
        x = raw_input()
        if x == '\q':
            printWithColor('quit', color_sys)
            break
        elif x == '\w':
            printWithColor('switch to word mode\n', color_sys)
            mode = 0
            continue
        print '--------------------'
        strs = re.split('\W+', x)
        for s in strs:
            printWithColor('Input:', color_sys, True)
            print '\b"' + s + '"\n',
            translator.translateWord(s, True)
        print ''

print '------------------------------------------'
printWithColor('Thank you for using Little Translator', color_error)
