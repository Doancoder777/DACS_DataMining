import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Load segment data
df = pd.read_csv('segment_benchmark.csv')

# Create chart like research paper
plt.figure(figsize=(12, 8))

# Use midpoint of each segment for x-axis
df['MidSupport'] = (df['MinSupport'] + df['MaxSupport']) / 2

algorithms = df['Algorithm'].unique()
colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd']
markers = ['o', 's', '^', 'D', 'v']

for i, algo in enumerate(algorithms):
    algo_data = df[df['Algorithm'] == algo].sort_values('MidSupport')
    plt.plot(algo_data['MidSupport'], algo_data['Runtime(ms)'] / 1000,
             marker=markers[i % len(markers)],
             color=colors[i % len(colors)],
             linewidth=2.5, markersize=8,
             label=algo)

plt.xlabel('Support Range Midpoint (%)', fontsize=12, fontweight='bold')
plt.ylabel('Runtime (seconds)', fontsize=12, fontweight='bold')
plt.title('Segment-wise Runtime Analysis - Rare Itemset Mining', fontsize=14, fontweight='bold')
plt.legend(loc='upper right')
plt.grid(True, alpha=0.3)
plt.gca().invert_xaxis()

# Add segment labels
segments = df[['MinSupport', 'MaxSupport']].drop_duplicates().sort_values('MinSupport')
for _, seg in segments.iterrows():
    mid = (seg['MinSupport'] + seg['MaxSupport']) / 2
    plt.axvline(x=mid, color='gray', linestyle='--', alpha=0.3)
    plt.text(mid, plt.ylim()[1] * 0.95, f"{seg['MinSupport']:.0f}-{seg['MaxSupport']:.0f}%",
             ha='center', va='top', fontsize=9, rotation=90)

plt.tight_layout()
plt.savefig('segment_runtime_analysis.png', dpi=300, bbox_inches='tight')
plt.show()

# Print segment summary
print('\n📊 SEGMENT SUMMARY:')
for _, seg in segments.iterrows():
    seg_data = df[(df['MinSupport'] == seg['MinSupport']) & (df['MaxSupport'] == seg['MaxSupport'])]
    avg_runtime = seg_data['Runtime(ms)'].mean()
    avg_patterns = seg_data['ItemsetsFound'].mean()
    print(f"{seg['MinSupport']:.0f}%-{seg['MaxSupport']:.0f}%: {avg_runtime:.0f}ms avg, {avg_patterns:.0f} patterns avg")
