import numpy
import matplotlib.pyplot as plt

binarySearchData = numpy.loadtxt("outFolder/learningTest.txt", delimiter=",", skiprows=1)
binarySearchData = numpy.flipud(binarySearchData)
plotData = binarySearchData[:,-2]
# odd rows are average
# even rows are std dev
avgData = plotData[1::2]
stdDevData = plotData[0::2]
upperLimitData = [avgData[i] + stdDevData[i] for i in range(len(avgData))]
lowerLimitData = [avgData[i] - stdDevData[i] for i in range(len(avgData))]
x = range(len(avgData))
x = [xx+1 for xx in x]
# shrink data for error bar
#samplingRate = 1
#avgData = avgData[::samplingRate]
#stdDevData = stdDevData[::samplingRate]
#x = x[::samplingRate]
#plt.errorbar(x, avgData, yerr=stdDevData)
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
