#coding:utf-8
import os
import sys

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
    strs = line.split(',')
    dic[strs[0]] = strs[1]
dic_file.close()

print 'please specify the input file'
filename = raw_input()

outputfile = file("output.txt", 'w')
if os.path.isfile(filename):
    inputfile = file(filename)
else:
    print 'failed loading input file: ' + filename + " doesn't exist"
    sys.exit(0)

while True:
    line = inputfile.readline()
    length = len(line)
    if length == 0:
        break
    pos = 0
    while pos < length:
        for end in range(pos, length):
            if line[pos:end] in dic:
                outputfile.write(line[pos:end] + '/')
                pos = end
                break
        outputfile.write(line[pos:length])
        pos = length
inputfile.close()
outputfile.close()
print "analizing finished, please check 'output.txt'"
