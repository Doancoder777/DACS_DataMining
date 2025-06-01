import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Load single support data
df = pd.read_csv('segment_benchmark.csv')

# Create chart like research paper with SINGLE support values
plt.figure(figsize=(12, 8))

algorithms = df['Algorithm'].unique()
colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd']
markers = ['o', 's', '^', 'D', 'v']

# Plot each algorithm
for i, algo in enumerate(algorithms):
    algo_data = df[df['Algorithm'] == algo].sort_values('SupportPoint')
    
    plt.plot(algo_data['SupportPoint'], algo_data['Runtime(ms)'] / 1000,
             marker=markers[i % len(markers)],
             color=colors[i % len(colors)],
             linewidth=2.5, markersize=8,
             label=algo, alpha=0.8)

plt.xlabel('Minimum Support (%)', fontsize=12, fontweight='bold')
plt.ylabel('Runtime (seconds)', fontsize=12, fontweight='bold')
plt.title('Runtime Analysis - Rare Itemset Mining\n(Single Support Values)', fontsize=14, fontweight='bold')
plt.legend(loc='upper right')
plt.grid(True, alpha=0.3)

# Invert x-axis to match research paper format
plt.gca().invert_xaxis()

# Add vertical lines at each support point
support_points = sorted(df['SupportPoint'].unique(), reverse=True)
for point in support_points:
    plt.axvline(x=point, color='gray', linestyle='--', alpha=0.3, linewidth=1)
    plt.text(point, plt.ylim()[1] * 0.95, f"{point:.0f}%",
             ha='center', va='top', fontsize=10, rotation=0,
             bbox=dict(boxstyle="round,pad=0.2", facecolor='white', alpha=0.7))

plt.tight_layout()
plt.savefig('runtime_analysis_single_support.png', dpi=300, bbox_inches='tight')
plt.show()

print('\n📊 Chart saved: runtime_analysis_single_support.png')
