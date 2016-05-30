import numpy
import matplotlib.pyplot as plt

binarySearchData = numpy.loadtxt("outFolder/learningTest.txt", delimiter=",", skiprows=1)
binarySearchData = numpy.flipud(binarySearchData)
plotData = binarySearchData[:,-2]
# running average
k = 50
indexLists = [range(n, n+k) for n in range(len(plotData)-k+1)]
for i in range(k-1):
    indexLists.append(indexLists[-1])
avgData = [numpy.mean(plotData[indexLists[i]]) for i in range(len(indexLists))]
upperLimitData = [numpy.max(plotData[indexLists[i]]) for i in range(len(indexLists))]
lowerLimitData = [numpy.min(plotData[indexLists[i]]) for i in range(len(indexLists))]
x = range(len(plotData))
x = [xx+1 for xx in x]
plt.plot(avgData)
plt.fill_between(x, upperLimitData, lowerLimitData, alpha=0.5)
plt.ylabel('Seller Utility')
plt.xlabel('Game Number')
plt.show()

'''
maxs = []
means = []
for i in range(0,11,1):
    fileName = "outFolder/learningTest" + "_" + str(float(i)/10).replace(".", "") + ".txt"
    binarySearchData = numpy.loadtxt(fileName, delimiter=",", skiprows=1)
    binarySearchData = numpy.flipud(binarySearchData)
    utilityData = binarySearchData[:,-2]
    maxs.append(max(utilityData))
    means.append(numpy.mean(utilityData))

xValues = [x/10.0 for x in range(11)]
#plt.plot(xValues, maxs)
plt.plot(xValues, means)
plt.ylabel('Seller Utility')
plt.xlabel('Game Number')
plt.show()
'''
