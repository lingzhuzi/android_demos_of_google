# encoding: utf-8
require 'rubygems'
require 'active_support'

json = ''
images = {}

File.open('json2') do |file|
  file.each do |line|
    json << line
  end
end

def is_image?(url)
  ['png', 'jpg', 'gif'].include?(url.split('.')[-1])
end

def create_files(files, images, path=nil)
  if path.nil?
    path = `pwd`
    path = path.gsub("\n", '/')
  end



  files.each do |name, value|
    if value.class.to_s == 'String'
      if is_image?(name)
        arr = value.split('/')
        full_name = "#{arr[-2]}/#{arr[-1]}"
        if images.include?(full_name)
          `
          mkdir -p #{path}
          cd #{path}
          cp #{images[full_name]} #{name}
          cd -
          `
        else
          `
        mkdir -p #{path}
        cd #{path}
        wget #{value}
        cd -
        `
          images[full_name] = "#{path}#{name}"
        end
      else
        `mkdir -p #{path}`
        fh = File.new("#{path}#{name}", "w") #创建一个可写文件流
        fh.puts value #写入数据
        fh.close #关闭文件流
        #    value.split(/\n/).each do |line|
        #     # `
        #     # cd #{path}
        #     # echo "#{line}" >> #{name}
        #     # `
        # end
      end
    else

      dir_path = path + "#{name.gsub(/\./, '/')}/"
      create_files(files[name], images, dir_path)
    end
  end
end

file_list = ActiveSupport::JSON.decode json
create_files(file_list, images)