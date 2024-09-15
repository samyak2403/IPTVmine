
# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2024-09-15
### Added
- Initial release of **Indian IPTVMine App** with M3U playlist support.
- **ChannelsProvider ViewModel** to fetch, parse, and filter channels based on user queries.
- **Search Feature** with a debounce mechanism for better filtering.
- Integrated **ExoPlayer** for seamless video streaming.
- Support for **HLS (HTTP Live Streaming)** media sources.
- Added **full-screen mode** and **orientation handling** in the video player.
- Basic **UI layout** for channel listing, player controls, and home screen.

### Changed
- Updated the **UI layout** to improve user experience, including:
  - Improved search functionality with better filtering options.
  - Redesigned **HomeFragment** for more intuitive navigation.
  - Updated **channel list display** to be more visually appealing.

### Fixed
- Resolved issues with video playback position handling.
- Fixed **NullPointerException** when loading specific channels from the playlist.
