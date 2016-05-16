import numpy
import matplotlib.pyplot as plt

'''
binarySearchData = numpy.loadtxt("outFolder/learningTest.txt", delimiter=",", skiprows=1)
binarySearchData = numpy.flipud(binarySearchData)
plt.plot(binarySearchData[:,-2])
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
