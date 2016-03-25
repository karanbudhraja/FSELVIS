import numpy
import matplotlib.pyplot as plt

'''
binarySearchData = numpy.loadtxt("outFolder/learningTest.txt", delimiter=",", skiprows=1)
binarySearchData = numpy.flipud(binarySearchData)
plt.plot(binarySearchData[:,-2])

plt.show()

print("max: " + str(max(binarySearchData[:,-2])))
print("mean: " + str(numpy.mean(binarySearchData[:,-2])))
'''

maxValues = []
avgValues = []
for i in range(0,11,1):
    f = i/10.0
    binarySearchData = numpy.loadtxt("outFolder/learningTest_" + str(f).replace(".", "") + ".txt", delimiter=",", skiprows=1)
    binarySearchData = numpy.flipud(binarySearchData)
    maxValue = max(binarySearchData[:,-2])
    avgValue = numpy.mean(binarySearchData[:,-2])
    maxValues.append(maxValue)
    avgValues.append(avgValue)

plt.plot(maxValues)
plt.plot(avgValues)
plt.xlabel("witness threshold (x10)")
plt.ylabel("max/avg utility")
plt.show()
