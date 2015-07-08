import pandas as pd
import matplotlib.pyplot as plt

import sys

fig = plt.figure()
ax = fig.add_subplot(111)
colors = ['r', 'g', 'b', 'c', 'm', 'y', 'k']
plt.rc('axes', color_cycle=colors)
df = pd.read_csv("TVNewsOutput.csv")
ax.set_xlabel("Lambda")
ax.set_ylabel("Root Mean Square Error")
ax.set_title("TVNewsChannels - Lambda vs Error")
p = ax.plot(df["Lambda"], df["TrainRMSE"], label="Train Data")
p = ax.plot(df["Lambda"], df["TestRMSE"],  label="Test Data")
plt.legend()
fig.show()

plot_image_file = "images/TVNewsChannelsOutput.png"
plt.savefig(plot_image_file, bbox_inches='tight')

raw_input("Press Enter to continue...")
sys.exit(0)

